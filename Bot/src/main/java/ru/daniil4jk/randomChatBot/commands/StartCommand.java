package ru.daniil4jk.randomChatBot.commands;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.KeyboardData;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;
import ru.daniil4jk.randomChatBot.keyboards.StartKeyboard;

import java.util.List;

@Slf4j
@Component
public class StartCommand extends BotCommand {
    @Autowired
    private BotConfig config;
    @Autowired
    @Getter
    private StartKeyboard startKeyboard;

    public StartCommand() {
        super("start", "Запустить бота " + Emoji.START);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public StartCommand(String commandIdentifier, String description, BotConfig config) {
        super(commandIdentifier, description);
        this.config = config;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        sendHelloMessage(absSender, user, chat.getId());
        reportRegistration(user);
    }

    private void sendHelloMessage(AbsSender absSender, User user, long chatID) {
        SendPhoto photoFirstMessage;
        try {
            photoFirstMessage = SendPhoto.builder()
                    .chatId(chatID)
                    .photo(((AbstractBot) absSender).getBufferedHelloPhoto())
                    .caption(getTextForHelloMessage(user))
                    .build();
        } catch (NullPointerException | ClassCastException e) {
            photoFirstMessage = null;
        }

        SendMessage secondMessage = SendMessage.builder().chatId(chatID)
                .text("Перед тем как начать общение, необходимо указать информацию о себе, чтобы я понимал кого тебе подбирать\uD83E\uDDD0")
                .replyMarkup(startKeyboard).build();

        try {
            try {
                absSender.execute(photoFirstMessage);
            } catch (NullPointerException | TelegramApiException e) {
                SendMessage simpleFirstMessage = SendMessage.builder()
                        .chatId(chatID)
                        .text(getTextForHelloMessage(user))
                        .build();

                absSender.execute(simpleFirstMessage);
            }

            Thread.sleep(200);

            absSender.execute(secondMessage);

        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        } catch (InterruptedException e) {
            log.error("Поток прерван во время отправки приветственного сообщения", e);
        }
    }

    private String getTextForHelloMessage(User user) {
        return "Привет, " + user.getFirstName() +
                "\uD83D\uDE0A ты попал(а) в самого лампового бота для анонимного общения в телеграмме";
    }

    private void reportRegistration(User user) {
        log.info("Зашел: @" + user.getUserName());
    }

    private void reportRegistrationToAdmin(AbsSender absSender, User user) {
        SendMessage errorMessage = SendMessage.builder()
                .chatId(config.getAdminUID())
                .text("Зашел user " + user.getUserName())
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }
}
