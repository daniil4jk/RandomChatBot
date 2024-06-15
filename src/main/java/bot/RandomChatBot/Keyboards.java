package bot.RandomChatBot;

import bot.RandomChatBot.models.UserProperties;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

class DefaultKeyboard extends ReplyKeyboardMarkup {
    private static final List<KeyboardRow> keyboard = new ArrayList<>();
    private static final KeyboardRow firstRow = new KeyboardRow();
    private static final KeyboardButton randomButton = new KeyboardButton(KeyboardData.RANDOM.getData());
    private static final KeyboardRow secondRow = new KeyboardRow();
    private static final KeyboardButton formButton = new KeyboardButton(KeyboardData.FORM.getData());
    private static final KeyboardButton settingsButton = new KeyboardButton(KeyboardData.SETTINGS.getData());
    private static final KeyboardRow thirdRow = new KeyboardRow();
    private static final KeyboardButton premiumButton = new KeyboardButton(KeyboardData.PREMIUM.getData());

    static {
        firstRow.add(randomButton);
        secondRow.add(formButton);
        secondRow.add(settingsButton);
        thirdRow.add(premiumButton);
        keyboard.add(firstRow);
        keyboard.add(secondRow);
        if (UserProperties.isPremiumSystemActive()) {
            keyboard.add(thirdRow);
        }
    }

    {
        setSelective(true);
        setResizeKeyboard(true);
        setOneTimeKeyboard(false);
    }

    public DefaultKeyboard() {
        this.setKeyboard(keyboard);
    }
}

class ChatKeyboard extends ReplyKeyboardMarkup {
    private static final List<KeyboardRow> keyboard = new ArrayList<>();
    private static final KeyboardRow firstRow = new KeyboardRow();
    private static final KeyboardButton stopButton = new KeyboardButton(KeyboardData.STOP.getData());
    private static final KeyboardRow secondRow = new KeyboardRow();
    private static final KeyboardButton friendButton = new KeyboardButton(KeyboardData.STOP.getData());

    static {
        firstRow.add(stopButton);
        keyboard.add(firstRow);
    }

    {
        setSelective(true);
        setResizeKeyboard(true);
        setOneTimeKeyboard(false);
    }

    public ChatKeyboard() {
        this.setKeyboard(keyboard);
    }
}