package ru.daniil4jk.randomChatBot.Commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.KeyboardData;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

import java.util.List;

@Slf4j
@Component
public class FormCommand extends ProtectedBotCommand {
    public FormCommand() {
        super("form", "Ваша анкета " + Emoji.FORM);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FormCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        RandomChatBotUser properties = getUsers().getRCBUser(chat.getId());
        SendMessage formMessage = SendMessage.builder()
                .chatId(chat.getId())
                .text("======= Ваша анкета =======\n\n    Ваш пол: " + properties.getGender().toRusString() + "  " + Emoji.GENDER +
                        "\n    Ваш возраст: " + properties.getAge() + " лет  " + Emoji.AGE +
                        "\n\n===========================")
                .replyMarkup(new PropertiesKeyboard())
                .build();
        try {
            absSender.execute(formMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    static class PropertiesKeyboard extends InlineKeyboardMarkup {
        private static final InlineKeyboardButton GENDER_BUTTON = new InlineKeyboardButton("Изменить свой пол" + Emoji.GENDER);
        private static final InlineKeyboardButton MY_AGE_BUTTON = new InlineKeyboardButton("Изменить свой возраст" + Emoji.AGE);
        private static final List<List<InlineKeyboardButton>> BUTTONS;

        static {
            GENDER_BUTTON.setCallbackData(KeyboardData.SET_GENDER.getStringValue());
            MY_AGE_BUTTON.setCallbackData(KeyboardData.SET_AGE.getStringValue());
            BUTTONS = List.of(List.of(GENDER_BUTTON),
                    List.of(MY_AGE_BUTTON));
        }

        public PropertiesKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

