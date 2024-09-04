package ru.daniil4jk.randomChatBot.bots;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;

public interface SimpleExecuter {
    <T extends Serializable, Method extends BotApiMethod<T>> T nonExceptionExecute(Method method);
    <T extends Serializable> T sendSimpleTextMessage(String messageText, long chatId);
}
