package ru.daniil4jk.randomChatBot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class FriendInviteKeyboard extends InlineKeyboardMarkup {
    private final InlineKeyboardButton YES = new InlineKeyboardButton(KeyboardData.FRIEND_ACCEPT.getStringValue());
    private final InlineKeyboardButton NO = new InlineKeyboardButton(KeyboardData.FRIEND_DENY.getStringValue());
    private final List<List<InlineKeyboardButton>> BUTTONS;

    public FriendInviteKeyboard() {
        YES.setCallbackData(KeyboardData.FRIEND_ACCEPT.getStringValue());
        NO.setCallbackData(KeyboardData.FRIEND_DENY.getStringValue());
        BUTTONS = List.of(List.of(YES, NO));
        this.setKeyboard(BUTTONS);
    }
}
