package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.FriendsKeyboard;

@Slf4j
@Component
public class ShowFriendsCommand extends ProtectedBotCommand {
    public ShowFriendsCommand() {
        super("friends", "Показать друзей");
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public ShowFriendsCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, long chatId, String[] strings) {
        try {
            if (getUserService().getUser(chatId).getFriends().isEmpty()) {
                absSender.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("У тебя пока что нет друзей " + Emoji.DOWNEYES + ",  но мы верим, ты их найдешь!")
                        .build());
            } else {
                absSender.execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("======= Твои друзья =======" +
                                "\n\n  Ты можешь отправить заявку " +
                                "\n  на общение любому из них, " +
                                "\n  и, если он(а) согласится - " +
                                "\n  я вас соединю " + Emoji.FRIENDS +
                                "\n\n===========================")
                        .replyMarkup(new FriendsKeyboard(chatId, getUserService()))
                        .build());
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
