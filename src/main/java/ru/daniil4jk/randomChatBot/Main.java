package ru.daniil4jk.randomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Date;

@SpringBootApplication
@EnableAutoConfiguration
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}

@Component
@Slf4j
class Initializer {
    @Autowired
    Bot bot;

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(bot);
        } catch (TelegramApiException e) {
            log.error("Регистрация бота не удалась", e);
        }
        bot.sayAboutLoad(100);
    }
}