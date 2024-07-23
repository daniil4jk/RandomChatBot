package ru.daniil4jk.randomChatBot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class FriendCallKeyboard extends InlineKeyboardMarkup {
    private final InlineKeyboardButton YES = new InlineKeyboardButton("Принять");
    private final InlineKeyboardButton NO = new InlineKeyboardButton("Отказать");

    public FriendCallKeyboard(long findingUID) {
        NO.setCallbackData("null");
        YES.setCallbackData("connect&" + findingUID);
        setKeyboard(List.of(List.of(YES, NO)));
    }
}
