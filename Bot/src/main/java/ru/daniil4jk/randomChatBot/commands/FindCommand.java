package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.models.Friend;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.NoSuchElementException;
import java.util.Random;

enum Name {
    REAL_NAME,
    FRIEND_NAME,
    ADMIN_NAME
}

@Slf4j
@Component
public class FindCommand extends ProtectedBotCommand {
    @Autowired
    BotConfig config;

    public FindCommand() {
        super("find", "Найти человека по никнейму)");
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FindCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, long chatId, @NotNull String[] strings) {
        try {
            if (UserService.OVERRIDE_ADMIN_PASS.equals(strings[1]) ||
                    UserService.OVERRIDE_USER_PASS.equals(strings[1]) && removeRequest(absSender, chatId)) {
                connect(absSender, chatId, strings);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {
            //делаем вид, что команда не существует, т.к. она введена неверно,
            //значит ее использовал не админ и не система
        }
    }

    private void connect(AbsSender absSender, long UID, @NotNull String[] strings) {
        try {
            long findingUID;
            try {
                findingUID = Long.parseLong(strings[0]);
            } catch (NumberFormatException e) {
                findingUID = getUserService().getUserByUsername(strings[0]).getID();
            }
            if (getUserService().pairs.containsKey(UID) || getUserService().pairs.containsKey(findingUID)) {
                throw new IllegalCallerException();
            }
            getUserService().pairs.put(UID, findingUID);
            getUserService().pairs.put(findingUID, UID);
            writeMessages(absSender, UID, findingUID, strings[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.unconnectedWriting, UID);
        } catch (NoSuchElementException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.unregisteredNickname, UID);
        } catch (IllegalCallerException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.busyUser, UID);
        }
    }

    private void writeMessages(AbsSender absSender, long UID, long findingUID, String pass) {
        boolean adminCall = UserService.OVERRIDE_ADMIN_PASS.equals(pass);
        Name showing_name = adminCall ? Name.REAL_NAME : Name.FRIEND_NAME;
        writeAboutConnection(absSender, UID, findingUID, showing_name);
        showing_name = adminCall ? Name.ADMIN_NAME : Name.FRIEND_NAME;
        writeAboutConnection(absSender, findingUID, UID, showing_name);
    }

    private void writeAboutConnection(AbsSender absSender, long UID, long findingUID, @NotNull Name name) {
        SendMessage message = new SendMessage();
        message.setChatId(UID);
        message.setText(switch (name) {
            case REAL_NAME -> "Вы подключились к " +
                    getUserService().getUser(findingUID).getUserName();
            case FRIEND_NAME -> "Вы подключились к " +
                    getFriendByUID(UID, findingUID).getName() + ")";
            case ADMIN_NAME -> "Вы подключились к " +
                    ("random".equals(config.getAdminNameForUsers()) ?
                            getRandomAdminName() : config.getAdminNameForUsers());
        });
        try {
            absSender.execute(message);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private String getRandomAdminName() {
        Random random = new Random(System.currentTimeMillis());
        byte[] bytes = new byte[random.nextInt(14)];
        random.nextBytes(bytes);
        char[] name = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            name[i] = (char) bytes[i];
        }
        return String.valueOf(name);
    }

    @NotNull
    private Friend getFriendByUID(long UID, long friendUID) {
        for (Friend f : getUserService().getUser(UID).getFriends()) {
            if (f.getId() == friendUID) {
                return f;
            }
        }
        throw new IllegalArgumentException("Не найден друг с ID " + friendUID + " у " + UID);
    }

    private boolean removeRequest(AbsSender absSender, long chatID) {
        if (getUserService().friendConnectRequests.containsKey(chatID)) {
            getUserService().friendConnectRequests.remove(chatID);
            return true;
        } else {
            try {
                absSender.execute(SendMessage.builder()
                        .chatId(chatID)
                        .text("К сожалению, эта заявка истекла или уже была использована " + Emoji.DOWNEYES)
                        .build());
            } catch (TelegramApiException e2) {
                log.warn("Не получилось отправить сообщение", e2);
            }
            return false;
        }
    }
}
