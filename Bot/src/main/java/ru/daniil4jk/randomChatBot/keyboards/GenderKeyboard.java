package ru.daniil4jk.randomChatBot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class GenderKeyboard extends InlineKeyboardMarkup {
    private final InlineKeyboardButton MALE_BUTTON = new InlineKeyboardButton("Парень");
    private final InlineKeyboardButton FEMALE_BUTTON = new InlineKeyboardButton("Девушка");
    private final List<List<InlineKeyboardButton>> BUTTONS;

    {
        MALE_BUTTON.setCallbackData(KeyboardData.SET_BOY_GENDER.getStringValue());
        FEMALE_BUTTON.setCallbackData(KeyboardData.SET_GIRL_GENDER.getStringValue());
        BUTTONS = List.of(List.of(MALE_BUTTON, FEMALE_BUTTON));
    }

    public GenderKeyboard() {
        this.setKeyboard(BUTTONS);
    }
}