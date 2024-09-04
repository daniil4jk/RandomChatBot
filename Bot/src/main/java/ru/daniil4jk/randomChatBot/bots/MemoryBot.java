package ru.daniil4jk.randomChatBot.bots;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.Serializable;

public abstract class MemoryBot extends AsyncLongPollingCommandBot{

    public MemoryBot(String token) {
        super(token);
    }


}
