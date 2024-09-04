package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.FriendCallKeyboard;
import ru.daniil4jk.randomChatBot.models.Friend;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class CallFriendCommand extends ProtectedBotCommand {
    public CallFriendCommand() {
        super("findcall", "Позвать друга пообщаться");
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public CallFriendCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, long chatId, String[] strings) {
        if (!UserService.OVERRIDE_USER_PASS.equals(strings[1])) {
            System.out.println("NO_RIGHTS");
            return;
        }
        try {
            long findingUID;
            try {
                findingUID = Long.parseLong(strings[0]);
            } catch (NumberFormatException e) {
                findingUID = getUserService().getUserByUsername(strings[0]).getID();
            }
            if (getUserService().pairs.containsKey(chatId) || getUserService().pairs.containsKey(findingUID)) {
                throw new IllegalCallerException();
            }
            callConnection(absSender, chatId, findingUID);
            getUserService().friendConnectRequests.put(findingUID, chatId);
            System.out.println(findingUID + " " + chatId);
        } catch (ArrayIndexOutOfBoundsException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.incorrectNickname, chatId);
        } catch (NoSuchElementException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.unregisteredNickname, chatId);
        } catch (IllegalCallerException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.busyUser, chatId);
        }
    }

    private void callConnection(@NotNull AbsSender absSender, long UID, long friendUID) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(UID)
                    .text("Заявка отправлена, ждем пока друг согласится " + Emoji.WINKING)
                    .build());
            String friendName;
            boolean hasName;
            try {
                friendName = getFriendByUID(friendUID, UID).getName();
                hasName = true;
            } catch (IllegalArgumentException e) {
                friendName = "(вы еще не подписали этого друга)";
                hasName = false;
            }
            absSender.execute(SendMessage.builder()
                    .chatId(friendUID)
                    .text("Твой друг " + friendName +
                            " приглашает вас пообщаться" + Emoji.CUTE_CLOSINGEYES)
                    .replyMarkup(new FriendCallKeyboard(UID))
                    .build());
            if (!hasName) {
                absSender.execute(SendMessage.builder()
                        .chatId(friendUID)
                        .text("Рекомендуем вам подписать друга, прежде чем раздумывать над заявкой, для этого просто напишите в чат его будущее имя")
                        .build());
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    @NotNull
    private Friend getFriendByUID(long UID, long findingUID) {
        for (Friend f : getUserService().getUser(UID).getFriends()) {
            if (f.getId() == findingUID) {
                return f;
            }
        }
        throw new IllegalArgumentException("Не найден друг с ID " + findingUID + " у " + UID);
    }
}
