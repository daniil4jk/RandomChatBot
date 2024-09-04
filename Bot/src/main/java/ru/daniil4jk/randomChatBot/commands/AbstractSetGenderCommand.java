package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public abstract class AbstractSetGenderCommand extends ProtectedBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public AbstractSetGenderCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(@NotNull AbsSender absSender, long chatId , String[] strings) {
        SendMessage askMessage =
                SendMessage.builder()
                        .text(getAskMessageText())
                        .chatId(chatId)
                        .replyMarkup(getKeyboardToSend())
                        .build();
        try {
            absSender.execute(askMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    protected abstract String getAskMessageText();

    protected abstract InlineKeyboardMarkup getKeyboardToSend();
}
