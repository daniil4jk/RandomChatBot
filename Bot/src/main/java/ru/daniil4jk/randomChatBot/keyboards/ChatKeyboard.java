package ru.daniil4jk.randomChatBot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatKeyboard extends ReplyKeyboardMarkup {
    private final List<KeyboardRow> keyboard = new ArrayList<>();
    private final KeyboardRow firstRow = new KeyboardRow();
    private final KeyboardButton stopButton = new KeyboardButton(KeyboardData.STOP.getStringValue());
    private final KeyboardRow secondRow = new KeyboardRow();
    private final KeyboardButton friendButton = new KeyboardButton(KeyboardData.ADD_FRIEND.getStringValue());

    {
        setSelective(true);
        setResizeKeyboard(true);
        setOneTimeKeyboard(false);
    }

    public ChatKeyboard() {
        firstRow.add(stopButton);
        secondRow.add(friendButton);
        keyboard.add(firstRow);
        keyboard.add(secondRow);
        this.setKeyboard(keyboard);
    }
}
