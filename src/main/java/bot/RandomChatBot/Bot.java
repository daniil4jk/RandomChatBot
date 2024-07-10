package bot.RandomChatBot;

import bot.RandomChatBot.Commands.*;
import bot.RandomChatBot.Constants.Emoji;
import bot.RandomChatBot.Constants.Gender;
import bot.RandomChatBot.Keyboards.KeyboardData;
import bot.RandomChatBot.service.BotConfig;
import bot.RandomChatBot.models.UserProperties;
import bot.RandomChatBot.service.UserService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Calendar;

import static bot.RandomChatBot.service.UserService.OVERRIDE;

@Slf4j
@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final BotConfig config;
    private final UserService users;
    private final BotCommand startCommand;
    private final BotCommand setAgeCommand;
    private final BotCommand setGenderCommand;
    private final BotCommand setFindingGenderCommand;
    private final BotCommand setMinFindingAgeCommand;
    private final BotCommand setMaxFindingAgeCommand;
    private final BotCommand findCommand;
    private final BotCommand randomCommand;
    private final BotCommand formCommand;
    private final BotCommand findSettingsCommand;
    private final BotCommand premiumCommand;
    private final BotCommand stopCommand;
    private final BotCommand helpCommand;

    public Bot(BotConfig config, UserService users) {
        super(config.getToken());
        this.config = config;
        this.users = users;
        register(setGenderCommand = new SetGenderCommand("setgender", "Выбрать пол " + Emoji.GENDER));
        register(startCommand = new StartCommand("start", "Запустить бота " + Emoji.START, this.config, setGenderCommand));
        register(setAgeCommand = new SetAgeCommand("setage", "Установить возраст " + Emoji.AGE));
        register(setFindingGenderCommand = new SetFindingGenderCommand("setfindinggender", "Указать желаемый пол " + Emoji.FINDING_GENDER));
        register(setMinFindingAgeCommand = new SetMinFindingAgeCommand("setminfindingage", "Указать мин. возраст поиска " + Emoji.MIN_FINDING_AGE));
        register(setMaxFindingAgeCommand = new SetMaxFindingAgeCommand("setmaxfindingage", "Указать макс. возраст поиска " + Emoji.MAX_FINDING_AGE));
        register(findCommand = new FindCommand("find", "Найти человека по никнейму)"));
        register(randomCommand = new FindRandomCommand("random", "Найти случайного собеседника " + Emoji.RANDOM));
        register(formCommand = new FormCommand("form", "Ваша анкета " + Emoji.FORM));
        register(findSettingsCommand = new FindSettingsCommand("findsettings", "Настройки поиска " + Emoji.SETTINGS));
        register(premiumCommand = new PremiumCommand("premium", "Платная подписка"));
        register(stopCommand = new StopCommand("stop", "Остановить чат " + Emoji.STOP));
        register(helpCommand = new HelpCommand("help", "Список всех команд " + Emoji.HELP));
        sayAboutStart();
    }

    private void sayAboutStart() {
        SendMessage message = new SendMessage();
        message.setChatId(config.getAdminUID());
        message.setText("Бот успешно запустился!");
        try {
            execute(message);
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

    private void processingMessage(Message message) {
        if (!message.getFrom().getId().equals(message.getChatId())) {
            Reports.reportGroupWriting(this, message.getChatId());
        }
        if (users.waitingMessageEvents.containsKey(message.getChatId())) {
            processingMessageEvents(message);
        } else if (message.hasText() && KeyboardData.contains(message.getText())) {
            processingMessageCommands(message);
        } else if (message.hasText() && users.pairs.containsKey(message.getChatId())) {
            copyMessage(message);
        } else if (message.hasSuccessfulPayment()) {
            servePayment(message.getSuccessfulPayment(), message.getChatId());
        } else if (message.hasDice()) {
            troll(message);
        } else {
            Reports.reportUnconnectedWriting(this, message.getChatId(), message.getFrom().getUserName());
        }
        if (message.hasPhoto() || message.hasDocument() ||
                message.hasVideo() || message.hasAudio() ||
                message.hasVoice()) {
            copyFileToAdminCheck(message);
        }
    }

    private void copyMessage(Message message) {
        users.getProperties(message.getChatId()).getMessages().add(message.getText());
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
        UserProperties userProperties = users.getProperties(message.getChatId());
        String fileID = null;
        if (message.hasPhoto()) fileID = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
        else if (message.hasVideo()) fileID = message.getVideo().getFileId();
        else if (message.hasVoice()) fileID = message.getVoice().getFileId();
        else if (message.hasDocument()) fileID = message.getDocument().getFileId();
        else if (message.hasAudio()) fileID = message.getAudio().getFileId();
        SendMessage caption;
        CopyMessage pic;
        try {
            pic = CopyMessage.builder()
                    .fromChatId(message.getChatId())
                    .chatId(config.getMediaGroupId())
                    .messageId(message.getMessageId())
                    .caption("От: @" + message.getFrom().getUserName() +
                            "\nС UserID: " + message.getFrom().getId() +
                            "\nПола: " + userProperties.getGender().toRusString() +
                            "\nВозраста: " + userProperties.getAge() +
                            "\nID файла: " + fileID)
                    .build();
        } catch (NullPointerException e) {
            return;
        }
        try {
            if (message.getCaption() != null) {
                caption = SendMessage.builder()
                        .text("@" + message.getFrom().getUserName() + ": " + message.getCaption())
                        .chatId(config.getMediaGroupId())
                        .build();
            } else {
                caption = null;
            }
        } catch (NullPointerException e) {
            caption = null;
        }
        try {
            if (caption != null) execute(caption);
            if (pic != null) execute(pic);
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

    private void processingKeyboardData(String conditionInString, User user, Chat chat) {
        KeyboardData condition = KeyboardData.getConst(conditionInString);
        switch (condition) {
            case START -> {
                if (users.exist(chat.getId()) && users.getProperties(chat.getId()).isRegistred()) {
                    writeAboutAlreadyRegistred(chat.getId());
                } else {
                    users.addUser(chat.getId());
                    setGenderCommand.execute(this, user, chat, new String[]{UserService.OVERRIDE});
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
                    setAgeCommand.execute(Bot.this, user, chat, new String[]{OVERRIDE});
                }
            }
            case SET_GIRL_GENDER -> {
                changeGender(chat.getId(), Gender.Girl, false);
                if (users.getProperties(chat.getId()).getAge() == -1) {
                    setAgeCommand.execute(Bot.this, user, chat, new String[]{OVERRIDE});
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
                    .execute(this, user, null, null);
            case SET_MAX_FIND_AGE -> setMaxFindingAgeCommand
                    .execute(this, user, null, null);
        }
    }

    private void processingMessageEvents(Message userMessage) {
        users.waitingMessageEvents.get(userMessage.getChatId()).accept(userMessage);
        users.waitingMessageEvents.remove(userMessage.getChatId());
    }

    private void changeGender(long chatID, Gender gender, boolean findingGender) {
        if (findingGender) {
            users.getProperties(chatID).setFindingGender(gender);
        } else {
            users.getProperties(chatID).setGender(gender);
        }
        writeAboutSuccessfullGenderChanging(chatID);
    }

    private void writeAboutSuccessfullGenderChanging(long chatID) {
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
}


