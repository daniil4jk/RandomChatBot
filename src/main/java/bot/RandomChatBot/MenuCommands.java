package bot.RandomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
class FormCommand extends UserInteractiveBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public FormCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        UserProperties properties = users.properties.get(user);
        SendMessage formMessage = SendMessage.builder()
                .chatId(user.getId())
                .text("=== Ваша анкета ===\n\n    Ваш пол: " + Gender.formatToRusString(properties.getGender()) + "  " + EmojiConstants.GENDER +
                        "\n    Ваш возраст: " + properties.getAge() + " лет  " + EmojiConstants.AGE +
                        "\n\n===================")
                .replyMarkup(new PropertiesKeyboard())
                .build();
        try {
            absSender.execute(formMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    static class PropertiesKeyboard extends InlineKeyboardMarkup {
        private static final InlineKeyboardButton GENDER_BUTTON = new InlineKeyboardButton("Изменить свой пол" + EmojiConstants.GENDER);
        private static final InlineKeyboardButton MY_AGE_BUTTON = new InlineKeyboardButton("Изменить свой возраст" + EmojiConstants.AGE);
        private static final List<List<InlineKeyboardButton>> BUTTONS;
        static {
            GENDER_BUTTON.setCallbackData(KeyboardConstants.SET_GENDER);
            MY_AGE_BUTTON.setCallbackData(KeyboardConstants.SET_AGE);
            BUTTONS = List.of(List.of(GENDER_BUTTON),
                    List.of(MY_AGE_BUTTON));
        }

        public PropertiesKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

@Slf4j
class FindSettingsCommand extends UserInteractiveBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public FindSettingsCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        if (users.properties.get(user).isPremium()) {
            try {
                absSender.execute(SendMessage.builder()
                        .chatId(user.getId())
                        .text(getForm(users.properties.get(user)))
                        .replyMarkup(new PropertiesKeyboard())
                        .build());
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
        } else {
            Reports.reportNeedPremium(absSender, user, "настройкам поиска");
        }
    }

    private String getForm(UserProperties properties) {
        return "=== Настройки поиска ===\n\n    Желаемый пол: " + Gender.formatToRusString(properties.getFindingGender()) + "  " + EmojiConstants.FINDING_GENDER +
                "\n    Мин. возраст для поиска: " + properties.getStartFindingAge() + " лет  " + EmojiConstants.MIN_FINDING_AGE +
                "\n    Макс. возраст для поиска: " + properties.getEndRequiredAge() + " лет  " + EmojiConstants.MAX_FINDING_AGE +
                "\n\n========================";
    }

    static class PropertiesKeyboard extends InlineKeyboardMarkup {
        private static final InlineKeyboardButton FIND_GENDER_BUTTON = new InlineKeyboardButton("Желаемый пол" + EmojiConstants.FINDING_GENDER);
        private static final InlineKeyboardButton FIND_MIN_AGE_BUTTON = new InlineKeyboardButton("Минимальный возраст для поиска" + EmojiConstants.MIN_FINDING_AGE);
        private static final InlineKeyboardButton FIND_MAX_AGE_BUTTON = new InlineKeyboardButton("Максимальный возраст для поиска" + EmojiConstants.MAX_FINDING_AGE);
        private static final List<List<InlineKeyboardButton>> BUTTONS;
        static {
            FIND_GENDER_BUTTON.setCallbackData(KeyboardConstants.SET_FINDING_GENDER);
            FIND_MIN_AGE_BUTTON.setCallbackData(KeyboardConstants.SET_MIN_FIND_AGE);
            FIND_MAX_AGE_BUTTON.setCallbackData(KeyboardConstants.SET_MAX_FIND_AGE);
            BUTTONS = List.of(List.of(FIND_GENDER_BUTTON),
                    List.of(FIND_MIN_AGE_BUTTON),
                    List.of(FIND_MAX_AGE_BUTTON));
        }

        public PropertiesKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

@Slf4j
class HelpCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public HelpCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        SendMessage help = SendMessage.builder()
                .text("====== Список всех команд ======" +
                        "\n\n/start - Запустить бота" + EmojiConstants.START +
                        "\n/form - Ваша анкета" + EmojiConstants.FORM +
                        "\n/setage - Установить возраст" + EmojiConstants.AGE +
                        "\n/setgender - Выбрать пол" + EmojiConstants.GENDER +
                        "\n/setfindinggender - Указать желаемый пол" + EmojiConstants.FINDING_GENDER +
                        "\n/setminfindingage - Указать мин. возраст поиска" + EmojiConstants.MIN_FINDING_AGE +
                        "\n/setmaxfindingage - Указать макс. возраст поиска" + EmojiConstants.MAX_FINDING_AGE +
                        "\n/random - Найти случайного собеседника" + EmojiConstants.RANDOM +
                        "\n/stop - Остановить чат" + EmojiConstants.STOP +
                        "\n/help - Список всех команд" + EmojiConstants.HELP +
                        "\n\n================================")
                .chatId(user.getId())
                .replyMarkup(new DefaultKeyboard())
                .build();
        try {
            absSender.execute(help);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
