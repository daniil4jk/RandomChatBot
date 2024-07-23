package ru.daniil4jk.randomChatBot.Commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.KeyboardData;
import ru.daniil4jk.randomChatBot.service.BotConfig;

import java.util.List;

@Slf4j
@Component
public class StartCommand extends UserServiceIntegratedBotCommand {
    @Autowired
    private BotConfig config;

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
        SendPhoto firstMessage = SendPhoto.builder().chatId(chatID)
                .caption("Привет, " + user.getFirstName() + "\uD83D\uDE0A ты попал(а) в самого лампового бота для анонимного общения в телеграмме")
                .photo(new InputFile("AgACAgIAAxkBAAIJk2Zjvk_Cf-4uznPnyMYMxwMpUe70AAKN2jEb_xUgS13GCak1cFjyAQADAgADeQADNQQ"))
                .build();
        SendMessage secondMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Перед тем как начать общение, необходимо указать информацию о себе, чтобы я понимал кого тебе подбирать\uD83E\uDDD0")
                .replyMarkup(new StartCommand.GoKey())
                .build();
        try {
            absSender.execute(firstMessage);
            Thread.sleep(200);
            absSender.execute(secondMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        } catch (InterruptedException e) {
            log.error("Поток прерван во время отправки приветственного сообщения", e);
        }
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

    static class GoKey extends InlineKeyboardMarkup {
        private final InlineKeyboardButton GO_BUTTON = new InlineKeyboardButton("ПОЕЕЕХАЛИИИИ");
        private final List<List<InlineKeyboardButton>> BUTTONS;

        {
            GO_BUTTON.setCallbackData(KeyboardData.START.getStringValue());
            BUTTONS = List.of(List.of(GO_BUTTON));
        }

        public GoKey() {
            this.setKeyboard(BUTTONS);
        }
    }
}
