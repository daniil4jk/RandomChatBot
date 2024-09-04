package ru.daniil4jk.randomChatBot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class StartKeyboard extends InlineKeyboardMarkup {
        private final InlineKeyboardButton GO_BUTTON = new InlineKeyboardButton("ПОЕЕЕХАЛИИИИ");
        private final List<List<InlineKeyboardButton>> BUTTONS;

        {
            GO_BUTTON.setCallbackData(KeyboardData.START.getStringValue());
            BUTTONS = List.of(List.of(GO_BUTTON));
        }

        public StartKeyboard() {
            this.setKeyboard(BUTTONS);
        }
}
