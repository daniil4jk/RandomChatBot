package ru.daniil4jk.randomChatBot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.daniil4jk.randomChatBot.service.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Component
public class DefaultKeyboard extends ReplyKeyboardMarkup {
    private final List<KeyboardRow> keyboard = new ArrayList<>();
    private final KeyboardRow firstRow = new KeyboardRow();
    private final KeyboardButton randomButton = new KeyboardButton(KeyboardData.RANDOM.getStringValue());
    private final KeyboardRow secondRow = new KeyboardRow();
    private final KeyboardButton formButton = new KeyboardButton(KeyboardData.FORM.getStringValue());
    private final KeyboardButton settingsButton = new KeyboardButton(KeyboardData.SETTINGS.getStringValue());
    private final KeyboardRow thirdRow = new KeyboardRow();
    private final KeyboardButton friendsButton = new KeyboardButton(KeyboardData.FRIENDS.getStringValue());
    private final KeyboardRow fourthRow = new KeyboardRow();
    private final KeyboardButton premiumButton = new KeyboardButton(KeyboardData.PREMIUM.getStringValue());

    {
        setSelective(true);
        setResizeKeyboard(true);
        setOneTimeKeyboard(false);
    }

    private DefaultKeyboard(BotConfig config) {
        firstRow.add(randomButton);
        secondRow.add(formButton);
        secondRow.add(settingsButton);
        thirdRow.add(friendsButton);
        fourthRow.add(premiumButton);
        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);
        if (config.isPremiumSystemActive()) {
            keyboard.add(fourthRow);
        }
        this.setKeyboard(keyboard);
    }
}

