package ru.daniil4jk.randomChatBot.bots;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class AsyncLongPollingBot extends TelegramLongPollingBot {
    ExecutorService pool = Executors.newFixedThreadPool(100);

    /** @deprecated */
    @Deprecated
    public AsyncLongPollingBot(DefaultBotOptions options) {
        super(options);
    }

    public AsyncLongPollingBot(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    @Override
    public final void onUpdateReceived(Update update) {
        try {
            pool.submit(() -> asyncUpdateHandler(update));
        } catch (Exception e) {
            log.error("Неизвестная ошибка", e);
        }
    }

    public abstract void asyncUpdateHandler(Update update);
}
