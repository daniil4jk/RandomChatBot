package ru.daniil4jk.randomChatBot;

import ru.daniil4jk.randomChatBot.Commands.*;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.keyboards.KeyboardData;
import ru.daniil4jk.randomChatBot.models.Friend;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.service.BotConfig;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.Calendar;
import java.util.function.Consumer;

@Slf4j
@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final AddFriendConsumer addFriendConsumer = new AddFriendConsumer();
    @Autowired
    private BotConfig config;
    @Autowired
    private UserService users;
    @Autowired
    private StartCommand startCommand;
    @Autowired
    private SetAgeCommand setAgeCommand;
    @Autowired
    private SetGenderCommand setGenderCommand;
    @Autowired
    private SetFindingGenderCommand setFindingGenderCommand;
    @Autowired
    private SetMinFindingAgeCommand setMinFindingAgeCommand;
    @Autowired
    private SetMaxFindingAgeCommand setMaxFindingAgeCommand;
    @Autowired
    private FindCommand findCommand;
    @Autowired
    private FindRandomCommand randomCommand;
    @Autowired
    private FormCommand formCommand;
    @Autowired
    private FindSettingsCommand findSettingsCommand;
    @Autowired
    private AddFriendCommand addFriendCommand;
    @Autowired
    private ShowFriendsCommand showFriendsCommand;
    @Autowired
    private FindCallCommand findCallCommand;
    @Autowired
    private PremiumCommand premiumCommand;
    @Autowired
    private StopCommand stopCommand;
    @Autowired
    private HelpCommand helpCommand;

    public Bot(BotConfig config, UserService users) {
        super(config.getToken());
        this.config = config;
        this.users = users;
    }

    @PostConstruct
    private void registerCommands() {
        register(setGenderCommand);
        register(startCommand);
        register(setAgeCommand);
        register(setFindingGenderCommand);
        register(setMinFindingAgeCommand);
        register(setMaxFindingAgeCommand);
        register(findCommand);
        register(randomCommand);
        register(formCommand);
        register(findSettingsCommand);
        register(addFriendCommand);
        register(showFriendsCommand);
        register(findCallCommand);
        register(premiumCommand);
        register(stopCommand);
        register(helpCommand);
        sayAboutLoad(90);
    }

    public void sayAboutLoad(int percentages) {
        log.info("Загрузка бота: " + percentages + "%");
        try {
            execute(SendMessage.builder()
                    .chatId(config.getAdminUID())
                    .text("Загрузка бота: " + percentages + "%")
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            processingMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            processingCallbackQuery(update.getCallbackQuery());
        } else if (update.hasPreCheckoutQuery()) {
            try {
                execute(new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true));
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
        }
    }

    private void processingMessage(@NotNull Message message) {
        if (!message.getFrom().getId().equals(message.getChatId())) {
            Reports.reportGroupWriting(this, message.getChatId());
        }
        if (users.messageEvents.containsKey(message.getChatId())) {
            processingMessageEvents(message);
        } else if (message.hasText() && KeyboardData.contains(message.getText())) {
            processingMessageCommands(message);
        } else if (message.hasText() && users.pairs.containsKey(message.getChatId())) {
            copyMessage(message);
            if (message.hasPhoto() || message.hasDocument() ||
                    message.hasVideo() || message.hasAudio() ||
                    message.hasVoice()) {
                copyFileToAdminCheck(message);
            }
        } else if (message.hasSuccessfulPayment()) {
            servePayment(message.getSuccessfulPayment(), message.getChatId());
        } else if (message.hasDice()) {
            troll(message);
        } else {
            Reports.reportUnconnectedWriting(this, message.getChatId());
        }
    }

    private void copyMessage(@NotNull Message message) {
        //users.getProperties(message.getChatId()).getMessages().add(message.getText()); TODO add messages to UserService
        CopyMessage m = CopyMessage.builder()
                .fromChatId(message.getChatId())
                .chatId(users.pairs.get(message.getChatId()))
                .messageId(message.getMessageId())
                .build();
        try {
            execute(m);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void copyFileToAdminCheck(Message message) {
        RandomChatBotUser RandomChatBotUser = users.getProperties(message.getChatId());
        String fileID = null;
        if (message.hasPhoto()) fileID = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
        else if (message.hasVideo()) fileID = message.getVideo().getFileId();
        else if (message.hasVoice()) fileID = message.getVoice().getFileId();
        else if (message.hasDocument()) fileID = message.getDocument().getFileId();
        else if (message.hasAudio()) fileID = message.getAudio().getFileId();
        CopyMessage file;
        try {
            file = CopyMessage.builder()
                    .fromChatId(message.getChatId())
                    .chatId(config.getMediaGroupId())
                    .messageId(message.getMessageId())
                    .caption("От: @" + message.getFrom().getUserName() +
                            "\nс UserID: " + message.getFrom().getId() +
                            "\nВ " + ("private".equals(message.getChat().getType()) ?
                            "приватном" : "многопользовательском") + " чате" +
                            "\nПол отправителя: " + RandomChatBotUser.getGender().toRusString() +
                            "\nВозраст отправителя: " + RandomChatBotUser.getAge() +
                            "\nID файла: " + fileID +
                            "\nПодпись: \n" + message.getCaption())
                    .build();
        } catch (NullPointerException e) {
            return;
        }
        try {
            if (file != null) execute(file);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void troll(Message message) {
        try {
            execute(SendMessage.builder()
                    .text("Пошутить решил? Одобряю. На кубике выпало " + message.getDice().getValue() + ")")
                    .chatId(message.getChatId())
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void servePayment(SuccessfulPayment payment, long chatID) {
        int days = Integer.parseInt(payment.getInvoicePayload());
        users.getProperties(chatID).addPremium(Calendar.DATE, days);
    }

    private void processingMessageCommands(Message message) {
        processingKeyboardData(message.getText(), message.getFrom(), message.getChat());
    }

    private void processingCallbackQuery(CallbackQuery callback) {
        @Nullable User user = users.UIDs.get(callback.getMessage().getChatId());
        processingKeyboardData(callback.getData(), user, new Chat(callback.getMessage().getChatId(), "private"));
    }

    private void processingKeyboardData(String keyboardData, User user, Chat chat) {
        if (keyboardData.startsWith("call&")) {
            findCallCommand.execute(this, user, chat,
                    new String[]{keyboardData.substring(5), UserService.OVERRIDE_USER_PASS});
        } else if (keyboardData.startsWith("connect&")) {
            findCommand.execute(this, user, chat,
                    new String[]{keyboardData.substring(8), UserService.OVERRIDE_USER_PASS});
        } else {
            keyboardActionSwitch(keyboardData, user, chat);
        }
    }

    private void keyboardActionSwitch(String keyboardData, User user, Chat chat) {
        KeyboardData condition = KeyboardData.getConst(keyboardData);
        switch (condition) {
            case START -> {
                if (users.exist(chat.getId()) && users.getProperties(chat.getId()).isRegistred()) {
                    writeAboutAlreadyRegistred(chat.getId());
                } else {
                    users.addUser(chat.getId());
                    setGenderCommand.execute(this, user, chat, new String[]{UserService.OVERRIDE_USER_PASS});
                }
            }
            case RANDOM -> randomCommand
                    .execute(this, user, chat, null);
            case FORM -> formCommand
                    .execute(this, user, chat, null);
            case SETTINGS -> findSettingsCommand
                    .execute(this, user, chat, null);
            case PREMIUM -> premiumCommand
                    .execute(this, user, chat, null);
            case STOP -> stopCommand
                    .execute(this, user, chat, null);
            case SET_BOY_GENDER -> {
                changeGender(chat.getId(), Gender.Boy, false);
                if (users.getProperties(chat.getId()).getAge() == -1) {
                    setAgeCommand.execute(Bot.this, user, chat, new String[]{UserService.OVERRIDE_USER_PASS});
                }
            }
            case SET_GIRL_GENDER -> {
                changeGender(chat.getId(), Gender.Girl, false);
                if (users.getProperties(chat.getId()).getAge() == -1) {
                    setAgeCommand.execute(Bot.this, user, chat, new String[]{UserService.OVERRIDE_USER_PASS});
                }
            }
            case SET_GENDER -> setGenderCommand
                    .execute(this, user, chat, null);
            case SET_AGE -> setAgeCommand
                    .execute(this, user, chat, null);
            case SET_FINDING_GENDER -> setFindingGenderCommand
                    .execute(this, user, chat, null);
            case SET_BOY_FINDING_GENDER -> changeGender(chat.getId(), Gender.Boy, true);
            case SET_GIRL_FINDING_GENDER -> changeGender(chat.getId(), Gender.Girl, true);
            case SET_MIN_FIND_AGE -> setMinFindingAgeCommand
                    .execute(this, user, chat, null);
            case SET_MAX_FIND_AGE -> setMaxFindingAgeCommand
                    .execute(this, user, chat, null);
            case ADD_FRIEND -> addFriendCommand
                    .execute(this, user, chat, null);
            case FRIENDS -> showFriendsCommand
                    .execute(this, user, chat, null);
            case FRIEND_ACCEPT -> {
                try {
                    writeAboutFriendWaitName(chat.getId(), users.friendRequests.get(chat.getId()));
                    users.messageEvents.put(chat.getId(), addFriendConsumer);
                    users.messageEvents.put(users.friendRequests.get(chat.getId()), addFriendConsumer);
                } catch (NullPointerException e) {
                    writeAboutInviteExpired(chat.getId());
                }
            }
            case FRIEND_DENY -> {
                try {
                    writeAboutFriendDeny(chat.getId());
                    long newFriendID = users.friendRequests.get(chat.getId());
                    users.friendRequests.remove(chat.getId());
                    users.friendRequests.remove(newFriendID);
                } catch (NullPointerException e) {
                    writeAboutInviteExpired(chat.getId());
                }
            }
            default ->
                    log.warn("switch обработки событий класиатуры не содержит нужного case", new NoSuchMethodException("Не найдено case для " + condition));
        }
    }

    private void processingMessageEvents(@NotNull Message userMessage) {
        users.messageEvents.get(userMessage.getChatId()).accept(userMessage);
        users.messageEvents.remove(userMessage.getChatId());
    }

    private void changeGender(long chatID, Gender gender, boolean findingGender) {
        if (findingGender) {
            users.getProperties(chatID).setFindingGender(gender);
        } else {
            users.getProperties(chatID).setGender(gender);
        }
        writeAboutSuccessfulGenderChanging(chatID);
    }

    private void writeAboutSuccessfulGenderChanging(long chatID) {
        SendMessage successMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Вы успешно установили пол" + Emoji.GENDER)
                .build();
        try {
            execute(successMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutAlreadyRegistred(long chatID) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Вы уже зарегистрированы\uD83D\uDE0A, нажимай на /random и погнали переписываться!")
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutFriendWaitName(long chatID, long friendChatID) {
        String basePartOne = ", напиши в чат имя, которым ты бы хотел";
        String basePartTwo = " назвать нового друга" + Emoji.WINKING;
        boolean isGirl = Gender.Girl.equals(users.getProperties(chatID).getGender());
        boolean isFriendGirl = Gender.Girl.equals(users.getProperties(friendChatID).getGender());
        try {
            execute(SendMessage.builder()
                    .text("Ты принял" + (isGirl ? "а" : "") + " заявку в друзья" +
                            basePartOne + (isGirl ? "а" : "") + basePartTwo)
                    .chatId(chatID)
                    .build());
            execute(SendMessage.builder()
                    .text("Твою заявку в друзья приняли" + basePartOne +
                            (isFriendGirl ? "а" : "") + basePartTwo)
                    .chatId(friendChatID)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutFriendsSuccessfullyAdded(long chatID) {
        boolean isGirl = Gender.Girl.equals(users.getProperties(chatID).getGender());
        try {
            execute(SendMessage.builder()
                    .text("Ты успешно добавил" + (isGirl ? "а" : "") + " нового друга, ура " + Emoji.CUTE_CLOSINGEYES)
                    .chatId(chatID)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutFriendDeny(long chatID) {
        try {
            execute(SendMessage.builder()
                    .chatId(users.friendRequests.get(chatID))
                    .text("Твою заявку в друзья отклонили \uD83D\uDE14, но не расстривайся, ты еще найдешь новых друзей☺\uFE0F")
                    .build());
            boolean isGirl = Gender.Girl.equals(users.getProperties(chatID).getGender());
            execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Ты отказал" + (isGirl ? "ась" : "ся") + " от заявки в друзья")
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutInviteExpired(long chatID) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("К сожалению, эта заявка истекла " + Emoji.DOWNEYES)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    class AddFriendConsumer implements Consumer<Message> {

        @Override
        public void accept(@NotNull Message s) {
            users.getProperties(s.getChatId()).getFriends()
                    .add(new Friend(s.getText(), users.friendRequests.get(s.getChatId())));
            writeAboutFriendsSuccessfullyAdded(s.getChatId());
            users.friendRequests.remove(s.getChatId());
        }
    }
}