package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.bots.SimpleExecuter;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.keyboards.FriendInviteKeyboard;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.models.Friend;

import java.util.List;

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

    //TODO добавить проверку, существует ли друг с таким именем, и если да - попросить ввести другое

    @Override
    public void protectedExecute(AbsSender absSender, long chatId, String[] strings) {
        long newFriendId = getUserService().pairs.get(chatId);
        if (!getUserService().pairs.containsKey(chatId)) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.unconnectedWriting, chatId);
        } else if (getUserService().getUser(chatId).getFriends().containsFriendWithId(newFriendId)) {
            ((SimpleExecuter) absSender).sendSimpleTextMessage(Reports.friendExist, chatId);
        } else  {
            tryAddFriend(absSender, chatId);
        }
    }

    private void tryAddFriend(AbsSender absSender, long chatId) {

        long newFriendChatId = getUserService().pairs.get(chatId);
        try {
            if (getUserService().friendRequests.containsKey(chatId) &&
                    getUserService().friendRequests.get(chatId) == newFriendChatId) {
                writeAboutInviteAlreadySent(absSender, chatId);
            } else {
                sendInvite(absSender, chatId, newFriendChatId);
                addInvite(chatId, newFriendChatId);
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutInviteAlreadySent(AbsSender absSender, long chatID) throws TelegramApiException {
        boolean isGirl = Gender.Girl.equals(getUserService().getUser(chatID).getGender());
        absSender.execute(SendMessage.builder()
                .chatId(chatID)
                .text("Ты уже отправлял" + (isGirl ? "а" : "") +
                        " заявку в друзья этому человеку (или получил" + (isGirl ? "а" : "") +
                        " заявку от собеседника), если ты отправлял" + (isGirl ? "а" : "") +
                        " заявку, дождись, пока он(она) ее примет " +
                        Emoji.WINKING)
                .build());
    }

    private void sendInvite(AbsSender absSender, long chatID, long newFriendChatID) throws TelegramApiException {
        absSender.execute(SendMessage.builder()
                .chatId(chatID)
                .text("Заявка на добавление в друзья отправлена! Ждем его(ее) положительного ответа)")
                .build());
        absSender.execute(SendMessage.builder()
                .chatId(newFriendChatID)
                .text("Тебе пришла заявка на добавление в друзья от человека, с которым ты сейчас общаешься " + Emoji.CRAZY + ", примешь ли ты ее?")
                .replyMarkup(new FriendInviteKeyboard())
                .build());
    }

    private void addInvite(long chatId, long newFriendChatId) {
        getUserService().friendRequests.put(chatId, newFriendChatId);
        getUserService().friendRequests.put(newFriendChatId, chatId);
    }
}
