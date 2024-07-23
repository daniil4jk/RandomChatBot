package ru.daniil4jk.randomChatBot.Commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.Reports;
import ru.daniil4jk.randomChatBot.constants.Emoji;

@Slf4j
@Component
public class StopCommand extends UserServiceIntegratedBotCommand {
    @Autowired
    ReplyKeyboardMarkup defaultKeyboard;

    public StopCommand() {
        super("stop", "Остановить чат " + Emoji.STOP);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public StopCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            long UID = chat.getId();
            long secondUID = getUsers().pairs.get(UID);
            writeDisconnectMessage(absSender, UID, secondUID);
            getUsers().pairs.remove(UID);
            writeDisconnectMessage(absSender, secondUID, UID);
            getUsers().pairs.remove(secondUID);
        } catch (NullPointerException e) {
            Reports.reportUnconnectedWriting(absSender, chat.getId());
        }
    }

    private void writeDisconnectMessage(AbsSender absSender, long UID, long secondUID) {
        SendMessage disconnectMessage = SendMessage.builder()
                .chatId(UID)
                .text("Вы успешно отсоединены от собеседника")
                .replyMarkup(defaultKeyboard)
                .build();
        log.trace("Пользователь " + getUsers().getRCBUser(UID) +
                " отсоединен от " + getUsers().getRCBUser(secondUID));
        try {
            absSender.execute(disconnectMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
