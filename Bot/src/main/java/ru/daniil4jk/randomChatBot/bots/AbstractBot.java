package ru.daniil4jk.randomChatBot.bots;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.daniil4jk.randomChatBot.controllers.NonCommandUpdateController;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;
import ru.daniil4jk.randomChatBot.keyboards.DefaultKeyboard;
import ru.daniil4jk.randomChatBot.service.CommandService;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class AbstractBot extends AsyncLongPollingCommandBot implements SimpleExecuter {
    String botName;
    InputFile helloPhoto;
    @Autowired
    private BotConfig config;
    @Autowired
    private UserService users;
    @Autowired
    private CommandService commands;
    @Autowired
    private DefaultKeyboard defaultKeyboard;
    private NonCommandUpdateController nonCommandUpdateController;

    public AbstractBot(String token, String botName) {
        super(token);
        this.botName = botName;
    }

    @PostConstruct
    private void createController() {
        nonCommandUpdateController = new NonCommandUpdateController(users, this, config, commands, defaultKeyboard);
    }

    @PostConstruct
    private void registerCommands() {
        registerAll(commands.getCommands());
        sayAboutLoad(90);
    }

    public void sayAboutLoad(int percentages) {
        log.info("Загрузка бота: " + percentages + "%");
        try {
            execute(SendMessage.builder()
                    .chatId(config.getAdminUID())
                    .text("Загрузка бота: " + percentages + "%")
                    .build());
        } catch (TelegramApiException e) {
            log.info("Не получилось отправить сообщение о загрузке администратору в лс");
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        nonCommandUpdateController.handleUpdate(update);
    }

    public abstract InputFile getHelloPhoto();

    public InputFile getBufferedHelloPhoto() {
        if (helloPhoto == null) {
            helloPhoto = getHelloPhoto();
        }
        return helloPhoto;
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T nonExceptionExecute(Method method) {
        try {
            return execute(method);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
        return null;
    }

    @Override
    public <T extends Serializable> T sendSimpleTextMessage(String messageText, long chatId) {
        return (T) nonExceptionExecute(SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .build());
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException {
        try {
            return super.execute(method);
        } catch (TelegramApiRequestException e) {
            if (!e.getMessage().contains("Too Many Requests")) {
                throw e;
            }
        }
        return null;
    }
}