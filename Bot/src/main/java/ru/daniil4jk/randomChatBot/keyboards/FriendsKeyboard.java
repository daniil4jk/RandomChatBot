package ru.daniil4jk.randomChatBot.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.daniil4jk.randomChatBot.models.Friend;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class FriendsKeyboard extends InlineKeyboardMarkup {

    public FriendsKeyboard(Long chatID, UserService service) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        boolean isRowHaveButton = false;
        List<InlineKeyboardButton> currentRow = null;
        for (Friend f : service.getUser(chatID).getFriends()) {
            if (isRowHaveButton) {
                currentRow.add(createKeyboardButton(f));
                isRowHaveButton = false;
            } else {
                currentRow = new ArrayList<>();
                currentRow.add(createKeyboardButton(f));
                keyboard.add(currentRow);
                isRowHaveButton = true;
            }
        }
        this.setKeyboard(keyboard);
    }

    private InlineKeyboardButton createKeyboardButton(Friend friend) {
        InlineKeyboardButton b = new InlineKeyboardButton(friend.getName() == null ||
                friend.getName().isEmpty() ? "Без имени" : friend.getName());
        b.setCallbackData("call&" + friend.getId());
        return b;
    }
}

