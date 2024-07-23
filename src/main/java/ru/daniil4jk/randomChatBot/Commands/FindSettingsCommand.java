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
import ru.daniil4jk.randomChatBot.Reports;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.KeyboardData;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

import java.util.List;

@Slf4j
@Component
public class FindSettingsCommand extends ProtectedBotCommand {
    public FindSettingsCommand() {
        super("findsettings", "Настройки поиска " + Emoji.SETTINGS);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FindSettingsCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (getUsers().getProperties(chat.getId()).isPremium()) {
            try {
                absSender.execute(SendMessage.builder()
                        .chatId(chat.getId())
                        .text(getForm(getUsers().getProperties(chat.getId())))
                        .replyMarkup(new PropertiesKeyboard())
                        .build());
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
        } else {
            Reports.reportNeedPremium(absSender, chat.getId(), "настройкам поиска");
        }
    }

    private String getForm(RandomChatBotUser properties) {
        return "=== Настройки поиска ===\n\n    Желаемый пол: " + properties.getFindingGender().toRusString() + "  " + Emoji.FINDING_GENDER +
                "\n    Мин. возраст для поиска: " + properties.getStartFindingAge() + " лет  " + Emoji.MIN_FINDING_AGE +
                "\n    Макс. возраст для поиска: " + properties.getEndRequiredAge() + " лет  " + Emoji.MAX_FINDING_AGE +
                "\n\n========================";
    }

    static class PropertiesKeyboard extends InlineKeyboardMarkup {
        private static final InlineKeyboardButton FIND_GENDER_BUTTON = new InlineKeyboardButton("Желаемый пол" + Emoji.FINDING_GENDER);
        private static final InlineKeyboardButton FIND_MIN_AGE_BUTTON = new InlineKeyboardButton("Минимальный возраст для поиска" + Emoji.MIN_FINDING_AGE);
        private static final InlineKeyboardButton FIND_MAX_AGE_BUTTON = new InlineKeyboardButton("Максимальный возраст для поиска" + Emoji.MAX_FINDING_AGE);
        private static final List<List<InlineKeyboardButton>> BUTTONS;

        static {
            FIND_GENDER_BUTTON.setCallbackData(KeyboardData.SET_FINDING_GENDER.getStringValue());
            FIND_MIN_AGE_BUTTON.setCallbackData(KeyboardData.SET_MIN_FIND_AGE.getStringValue());
            FIND_MAX_AGE_BUTTON.setCallbackData(KeyboardData.SET_MAX_FIND_AGE.getStringValue());
            BUTTONS = List.of(List.of(FIND_GENDER_BUTTON),
                    List.of(FIND_MIN_AGE_BUTTON),
                    List.of(FIND_MAX_AGE_BUTTON));
        }

        public PropertiesKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}
