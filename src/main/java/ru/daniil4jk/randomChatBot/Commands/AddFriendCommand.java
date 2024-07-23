package ru.daniil4jk.randomChatBot.Commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.Reports;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.keyboards.FriendInviteKeyboard;

@Slf4j
@Component
public class AddFriendCommand extends ProtectedBotCommand {
    @Autowired
    FriendInviteKeyboard friendInviteKeyboard;

    public AddFriendCommand() {
        super("addFriend", "Добавить друга");
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public AddFriendCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            //TODO добавить проверку на то, существует ли этот друг уже в списке друзей
            if (!getUsers().pairs.containsKey(chat.getId())) {
                Reports.reportUnconnectedWriting(absSender, chat.getId());
            } else if (getUsers().friendRequests.containsKey(chat.getId())) {
                writeAboutInviteAlreadySent(absSender, chat.getId());
            } else {
                sendInvite(absSender, chat.getId());
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutInviteAlreadySent(AbsSender absSender, long chatID) throws TelegramApiException {
        boolean isGirl = Gender.Girl.equals(getUsers().getRCBUser(chatID).getGender());
        absSender.execute(SendMessage.builder()
                .chatId(chatID)
                .text("Ты уже отправлял" + (isGirl ? "а" : "") +
                        " заявку в друзья этому человеку (или получил" + (isGirl ? "а" : "") +
                        " заявку от собеседника), если ты отправлял" + (isGirl ? "а" : "") +
                        " заявку, дождись, пока он(она) ее примет " +
                        Emoji.WINKING)
                .build());
    }

    private void sendInvite(AbsSender absSender, long chatID) throws TelegramApiException {
        absSender.execute(SendMessage.builder()
                .chatId(chatID)
                .text("Заявка на добавление в друзья отправлена! Ждем его(ее) положительного ответа)")
                .build());
        long newFriendChatID = getUsers().pairs.get(chatID);
        getUsers().friendRequests.put(chatID, newFriendChatID);
        getUsers().friendRequests.put(newFriendChatID, chatID);
        absSender.execute(SendMessage.builder()
                .chatId(newFriendChatID)
                .text("Тебе пришла заявка на добавление в друзья от человека, с которым ты сейчас общаешься " + Emoji.CRAZY + ", примешь ли ты ее?")
                .replyMarkup(new FriendInviteKeyboard())
                .build());
    }
}
