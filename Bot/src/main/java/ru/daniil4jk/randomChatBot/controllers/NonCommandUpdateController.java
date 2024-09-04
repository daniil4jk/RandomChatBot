package ru.daniil4jk.randomChatBot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.commands.*;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.keyboards.DefaultKeyboard;
import ru.daniil4jk.randomChatBot.keyboards.KeyboardData;
import ru.daniil4jk.randomChatBot.models.Friend;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;
import ru.daniil4jk.randomChatBot.service.CommandService;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.function.Consumer;

@Slf4j
public class NonCommandUpdateController {
    private final UserService users;
    private final AbstractBot bot;
    private final BotConfig config;
    private final CommandService commands;
    private final DefaultKeyboard defaultKeyboard;

    public NonCommandUpdateController(UserService users, AbstractBot bot,
                                      BotConfig config, CommandService commands,
                                      DefaultKeyboard defaultKeyboard) {
        this.users = users;
        this.bot = bot;
        this.config = config;
        this.commands = commands;
        this.defaultKeyboard = defaultKeyboard;
    }

    public void handleUpdate(@NotNull Update update) {
        if (update.hasMessage()) {
            processingMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            processingCallbackQuery(update.getCallbackQuery());
        } else if (update.hasPreCheckoutQuery()) {
            try {
                bot.execute(new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true));
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
        }
    }

    private void processingMessage(@NotNull Message message) {
        if (!message.getFrom().getId().equals(message.getChatId())) {
            bot.sendSimpleTextMessage(Reports.groupWriting, message.getChatId());
        }
        if (!users.messageEvents.computeIfAbsent(message.getChatId(), k -> new LinkedList<>()).isEmpty()) {
            processingMessageEvents(message);
        } else if (message.hasText() && KeyboardData.contains(message.getText())) {
            processingMessageCommands(message);
        } else if (message.hasSuccessfulPayment()) {
            servePayment(message.getSuccessfulPayment(), message.getChatId());
        } else if (users.pairs.containsKey(message.getChatId())) {
            copyMessage(message);
            if (message.hasPhoto() || message.hasDocument() ||
                    message.hasVideo() || message.hasAudio() ||
                    message.hasVoice()) {
                copyFileToAdminCheck(message);
            }
        } else if (message.hasDice()) {
            troll(message);
        } else {
            bot.sendSimpleTextMessage(Reports.unconnectedWriting, message.getChatId());
        }
    }

    private void copyMessage(@NotNull Message message) {
        CopyMessage m = CopyMessage.builder()
                .fromChatId(message.getChatId())
                .chatId(users.pairs.get(message.getChatId()))
                .messageId(message.getMessageId())
                .build();
        try {
            bot.execute(m);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void copyFileToAdminCheck(@NotNull Message message) {
        RandomChatBotUser RandomChatBotUser = users.getUser(message.getChatId());
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
            if (file != null) bot.execute(file);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void troll(Message message) {
        try {
            bot.execute(SendMessage.builder()
                    .text("Пошутить решил? Одобряю. На кубике выпало " + message.getDice().getValue() + ")")
                    .chatId(message.getChatId())
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void servePayment(SuccessfulPayment payment, long chatID) {
        int days = Integer.parseInt(payment.getInvoicePayload());
        users.getUser(chatID).addPremium(Calendar.DATE, days);
    }

    private void processingMessageCommands(Message message) {
        processingKeyboardData(message.getText(), message.getChatId());
    }

    private void processingCallbackQuery(CallbackQuery callback) {
        processingKeyboardData(callback.getData(), callback.getMessage().getChatId());
    }

    private void processingKeyboardData(String keyboardData, long chatId) {
        if (keyboardData.startsWith("call&")) {
            commands.getCommand(CallFriendCommand.class).execute(bot, chatId,
                    new String[]{keyboardData.substring(5), UserService.OVERRIDE_USER_PASS});
        } else if (keyboardData.startsWith("connect&")) {
            commands.getCommand(FindCommand.class).execute(bot, chatId,
                    new String[]{keyboardData.substring(8), UserService.OVERRIDE_USER_PASS});
        } else {
            keyboardActionSwitch(keyboardData, chatId);
        }
    }

    private void keyboardActionSwitch(String keyboardData, long chatId) {
        KeyboardData condition = KeyboardData.getConst(keyboardData);
        switch (condition) {
            case START -> {
                if (users.contains(chatId) && users.getUser(chatId).isRegistred()) {
                    writeAboutAlreadyRegistred(chatId);
                } else {
                    users.addUser(chatId);
                    commands.getCommand(SetGenderCommand.class).execute(bot, chatId, new String[]{UserService.OVERRIDE_USER_PASS});
                }
            }
            case RANDOM -> commands.getCommand(FindRandomCommand.class)
                    .execute(bot, chatId, null);
            case FORM -> commands.getCommand(FormCommand.class)
                    .execute(bot, chatId, null);
            case SETTINGS -> commands.getCommand(FindSettingsCommand.class)
                    .execute(bot, chatId, null);
            case PREMIUM -> commands.getCommand(PremiumCommand.class)
                    .execute(bot, chatId, null);
            case STOP -> commands.getCommand(StopCommand.class)
                    .execute(bot, chatId, null);
            case SET_BOY_GENDER -> {
                changeGender(chatId, Gender.Boy, false);
                if (users.getUser(chatId).getAge() == -1) {
                    commands.getCommand(SetAgeCommand.class).execute(bot, chatId, new String[]{UserService.OVERRIDE_USER_PASS});
                }
            }
            case SET_GIRL_GENDER -> {
                changeGender(chatId, Gender.Girl, false);
                if (users.getUser(chatId).getAge() == -1) {
                    commands.getCommand(SetAgeCommand.class).execute(bot, chatId, new String[]{UserService.OVERRIDE_USER_PASS});
                }
            }
            case SET_GENDER -> commands.getCommand(SetGenderCommand.class)
                    .execute(bot, chatId, null);
            case SET_AGE -> commands.getCommand(SetAgeCommand.class)
                    .execute(bot, chatId, null);
            case SET_FINDING_GENDER -> commands.getCommand(SetFindingGenderCommand.class)
                    .execute(bot, chatId, null);
            case SET_BOY_FINDING_GENDER -> changeGender(chatId, Gender.Boy, true);
            case SET_GIRL_FINDING_GENDER -> changeGender(chatId, Gender.Girl, true);
            case SET_MIN_FIND_AGE -> commands.getCommand(SetMinFindingAgeCommand.class)
                    .execute(bot, chatId, null);
            case SET_MAX_FIND_AGE -> commands.getCommand(SetMaxFindingAgeCommand.class)
                    .execute(bot, chatId, null);
            case ADD_FRIEND -> commands.getCommand(AddFriendCommand.class)
                    .execute(bot, chatId, null);
            case FRIENDS -> commands.getCommand(ShowFriendsCommand.class)
                    .execute(bot, chatId, null);
            case FRIEND_ACCEPT -> {
                try {
                    writeAboutFriendWaitName(chatId, users.friendRequests.get(chatId));
                    users.messageEvents.get(chatId).add(addFriendConsumer);
                    users.messageEvents.get(users.friendRequests.get(chatId)).add(addFriendConsumer);
                } catch (NullPointerException e) {
                    writeAboutInviteExpired(chatId);
                }
            }
            case FRIEND_DENY -> {
                try {
                    writeAboutFriendDeny(chatId);
                    long newFriendID = users.friendRequests.get(chatId);
                    users.friendRequests.remove(chatId);
                    users.friendRequests.remove(newFriendID);
                } catch (NullPointerException e) {
                    writeAboutInviteExpired(chatId);
                }
            }
            default -> log.warn("switch обработки событий класиатуры не содержит нужного case",
                    new NoSuchMethodException("Не найдено case для " + condition));
        }
    }

    private void processingMessageEvents(@NotNull Message userMessage) {
        users.messageEvents.get(userMessage.getChatId()).remove().accept(userMessage);
    }

    private void changeGender(long chatID, Gender gender, boolean findingGender) {
        if (findingGender) {
            users.getUser(chatID).setFindingGender(gender);
        } else {
            users.getUser(chatID).setGender(gender);
        }
        writeAboutSuccessfulGenderChanging(chatID);
    }

    private void writeAboutSuccessfulGenderChanging(long chatID) {
        SendMessage successMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Вы успешно установили пол" + Emoji.GENDER)
                .build();
        try {
            bot.execute(successMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutAlreadyRegistred(long chatID) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Вы уже зарегистрированы\uD83D\uDE0A, нажимай на /random и погнали переписываться!")
                    .replyMarkup(defaultKeyboard)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutFriendWaitName(long chatID, long friendChatID) {
        String basePartOne = ", напиши в чат имя, которым ты бы хотел";
        String basePartTwo = " назвать нового друга" + Emoji.WINKING;
        boolean isGirl = Gender.Girl.equals(users.getUser(chatID).getGender());
        boolean isFriendGirl = Gender.Girl.equals(users.getUser(friendChatID).getGender());
        try {
            bot.execute(SendMessage.builder()
                    .text("Ты принял" + (isGirl ? "а" : "") + " заявку в друзья" +
                            basePartOne + (isGirl ? "а" : "") + basePartTwo)
                    .chatId(chatID)
                    .build());
            bot.execute(SendMessage.builder()
                    .text("Твою заявку в друзья приняли" + basePartOne +
                            (isFriendGirl ? "а" : "") + basePartTwo)
                    .chatId(friendChatID)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutFriendsSuccessfullyAdded(long chatID) {
        boolean isGirl = Gender.Girl.equals(users.getUser(chatID).getGender());
        try {
            bot.execute(SendMessage.builder()
                    .text("Ты успешно добавил" + (isGirl ? "а" : "") + " нового друга, ура " + Emoji.CUTE_CLOSINGEYES)
                    .chatId(chatID)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutFriendDeny(long chatID) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(users.friendRequests.get(chatID))
                    .text("Твою заявку в друзья отклонили \uD83D\uDE14, но не расстривайся, ты еще найдешь новых друзей☺\uFE0F")
                    .build());
            boolean isGirl = Gender.Girl.equals(users.getUser(chatID).getGender());
            bot.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Ты отказал" + (isGirl ? "ась" : "ся") + " от заявки в друзья")
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutInviteExpired(long chatID) {
        try {
            bot.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("К сожалению, эта заявка истекла " + Emoji.DOWNEYES)
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    AddFriendConsumer addFriendConsumer = new AddFriendConsumer();

    class AddFriendConsumer implements Consumer<Message> {

        @Override
        public void accept(@NotNull Message s) {
            users.getUser(s.getChatId()).getFriends()
                    .add(new Friend(s.getText(),
                            users.friendRequests.get(s.getChatId()),
                            users.getUser(s.getChatId())));
            writeAboutFriendsSuccessfullyAdded(s.getChatId());
            users.friendRequests.remove(s.getChatId());
        }
    }
}
