package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.constants.Reports;

import java.util.LinkedList;

@Slf4j
public abstract class AbstractSetAgeCommand extends ProtectedBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public AbstractSetAgeCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(@NotNull AbsSender absSender, long chatId, String[] strings) {
        SendMessage askMessage = SendMessage.builder()
                .chatId(chatId)
                .text(getAskMessageText())
                .build();
        try {
            absSender.execute(askMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
        addSetAgeEventToQueue(absSender, chatId);
    }

    private void addSetAgeEventToQueue(AbsSender absSender, long chatId) {
        getUserService().messageEvents.computeIfAbsent(chatId, k -> new LinkedList<>())
                .add(messageWithAge -> {
            trySetAge(absSender, messageWithAge);
            additionalEvents(absSender, messageWithAge);
        });
    }

    public void additionalEvents(AbsSender absSender, Message message) {};

    private void trySetAge(AbsSender absSender, Message messageWithAge) {
        try {
            protectedSetAge(absSender, messageWithAge);
        } catch (IllegalArgumentException e) {
            getUserService().messageEvents.get(messageWithAge.getChatId()).add(
                    newMessageWithAge -> trySetAge(absSender, newMessageWithAge));
        }
    }

    private void protectedSetAge(AbsSender absSender, Message messageWithAge) {
        try {
            int age = Integer.parseInt(messageWithAge.getText());
            if (age > 120 || age < 1) {
                throw new IllegalArgumentException();
            }
            setAge(messageWithAge.getChatId(), age);
            SendMessage successMessage = SendMessage.builder()
                    .chatId(messageWithAge.getChatId())
                    .text("Вы успешно установили возраст" + Emoji.AGE)
                    .build();
            absSender.execute(successMessage);
        } catch (NumberFormatException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.notNumber,  messageWithAge.getChatId());
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            Gender gender = getUserService().getUser(messageWithAge.getChatId()).getGender();
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.illegalAge(gender),  messageWithAge.getChatId());
            throw e;
        } catch (NullPointerException e) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.emptyAge,  messageWithAge.getChatId());
            throw new IllegalArgumentException();
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    protected abstract void setAge(long chatId, int age);

    protected abstract String getAskMessageText();
}
