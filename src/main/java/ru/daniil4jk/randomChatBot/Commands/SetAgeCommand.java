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
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

@Slf4j
@Component
public class SetAgeCommand extends ProtectedBotCommand {
    @Autowired
    ReplyKeyboardMarkup defaultKeyboard;

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
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage askMessage = SendMessage.builder()
                .chatId(chat.getId())
                .text(getMessageText())
                .build();
        try {
            absSender.execute(askMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
        getUsers().messageEvents.put(chat.getId(), s -> {
            try {
                setAge(getUsers().getProperties(s.getChatId()), Integer.parseInt(s.getText()));
                SendMessage successMessage = SendMessage.builder()
                        .chatId(s.getChatId())
                        .text("Вы успешно установили возраст" + Emoji.AGE)
                        .build();
                absSender.execute(successMessage);
            } catch (NumberFormatException e) {
                Reports.reportNotNumber(absSender, s.getChatId());
            } catch (NullPointerException e) {
                Reports.reportEmptyAge(absSender, s.getChatId());
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
            if (!getUsers().getProperties(s.getChatId()).isRegistred()) {
                getUsers().getProperties(s.getChatId()).setRegistred(true);
                getUsers().getProperties(s.getChatId()).setUserName(s.getFrom().getUserName());
                getUsers().UIDs.put(s.getChatId(), s.getFrom());
                writeAboutSuccessfullRegistration(absSender, s.getChatId());
            }
        });
    }

    private void writeAboutSuccessfullRegistration(AbsSender absSender, long chatID) {
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

    protected String getMessageText() {
        return "Введите ваш возраст\uD83D\uDE09";
    }

    protected RandomChatBotUser setAge(RandomChatBotUser properties, int age) {
        properties.setAge(age);
        return properties;
    }
}
