package ru.daniil4jk.randomChatBot.Commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.Reports;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.FriendCallKeyboard;
import ru.daniil4jk.randomChatBot.models.Friend;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class FindCallCommand extends ProtectedBotCommand {
    public FindCallCommand() {
        super("findcall", "Позвать друга пообщаться");
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FindCallCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!UserService.OVERRIDE_USER_PASS.equals(strings[1])) {
            System.out.println("NO_RIGHTS");
            return;
        }
        try {
            long UID = chat.getId();
            long findingUID;
            try {
                findingUID = Long.parseLong(strings[0]);
            } catch (NumberFormatException e) {
                findingUID = getUsers().getPropertiesByUserName(strings[0]).getID();
            }
            if (getUsers().pairs.containsKey(UID) || getUsers().pairs.containsKey(findingUID)) {
                throw new IllegalCallerException();
            }
            callConnection(absSender, UID, findingUID);
            getUsers().friendConnectRequests.put(findingUID, UID);
            System.out.println(findingUID + " " + UID);
        } catch (ArrayIndexOutOfBoundsException e) {
            Reports.reportIncorrectNickname(absSender, chat.getId(), null);
        } catch (NoSuchElementException e) {
            Reports.reportUnregisteredNickname(absSender, chat.getId(), strings[0]);
        } catch (IllegalCallerException e) {
            Reports.reportBusyUser(absSender, chat.getId(), strings[0]);
        }
    }

    private void callConnection(@NotNull AbsSender absSender, long UID, long findingUID) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(UID)
                    .text("Заявка отправлена, ждем пока друг согласится " + Emoji.WINKING)
                    .build());
            absSender.execute(SendMessage.builder()
                    .chatId(findingUID)
                    .text("Твой друг " + getFriendByUID(findingUID, UID).getName() +
                            " приглашает вас пообщаться" + Emoji.CUTE_CLOSINGEYES)
                    .replyMarkup(new FriendCallKeyboard(UID))
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    @NotNull
    private Friend getFriendByUID(long UID, long friendUID) {
        for (Friend f : getUsers().getRCBUser(UID).getFriends()) {
            if (f.getTelegramId() == friendUID) {
                return f;
            }
        }
        throw new IllegalArgumentException("Не найден друг с ID " + friendUID + " у " + UID);
    }
}
