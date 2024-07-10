package bot.RandomChatBot.Keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ChatKeyboard extends ReplyKeyboardMarkup {
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
