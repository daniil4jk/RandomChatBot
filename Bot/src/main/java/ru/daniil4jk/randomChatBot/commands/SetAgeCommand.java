package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.DefaultKeyboard;

@Slf4j
@Component
public class SetAgeCommand extends AbstractSetAgeCommand {
    @Autowired
    DefaultKeyboard defaultKeyboard;

    public SetAgeCommand() {
        super("setage", "Установить возраст " + Emoji.AGE);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public SetAgeCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void additionalEvents(AbsSender absSender, Message messageWithAge) {
        if (!getUserService().getUser(messageWithAge.getChatId()).isRegistred()) {
            registrationComplete(absSender, messageWithAge);
        }
    }

    private void registrationComplete(AbsSender absSender, @NotNull Message messageWithAge) {
        getUserService().getUser(messageWithAge.getChatId()).setRegistred(true);
        getUserService().getUser(messageWithAge.getChatId()).setUserName(messageWithAge.getFrom().getUserName());
        writeAboutSuccessfullRegistration(absSender, messageWithAge.getChatId());
    }

    private void writeAboutSuccessfullRegistration(@NotNull AbsSender absSender, long chatID) {
        SendMessage successMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Вы успешно зарегистрированы, нажимайте /random и погнали чатиться\uD83E\uDD73)")
                .replyMarkup(defaultKeyboard)
                .build();
        try {
            absSender.execute(successMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    @Override
    protected void setAge(long chatId, int age) {
        getUserService().getUser(chatId).setAge(age);
    }

    @Override
    protected String getAskMessageText() {
        return "Введите ваш возраст";
    }
}
