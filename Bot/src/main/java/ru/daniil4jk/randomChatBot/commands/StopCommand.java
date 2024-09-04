package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Reports;

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
    public void execute(AbsSender absSender, long chatId, String[] strings) {
        try {
            long secondUID = getUserService().pairs.get(chatId);
            writeDisconnectMessage(absSender, chatId, secondUID);
            getUserService().pairs.remove(chatId);
            writeDisconnectMessage(absSender, secondUID, chatId);
            getUserService().pairs.remove(secondUID);
        } catch (NullPointerException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.unconnectedWriting, chatId);
        }
    }

    private void writeDisconnectMessage(AbsSender absSender, long UID, long secondUID) {
        SendMessage disconnectMessage = SendMessage.builder()
                .chatId(UID)
                .text("Вы успешно отсоединены от собеседника")
                .replyMarkup(defaultKeyboard)
                .build();
        log.trace("Пользователь " + getUserService().getUser(UID) +
                " отсоединен от " + getUserService().getUser(secondUID));
        try {
            absSender.execute(disconnectMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
