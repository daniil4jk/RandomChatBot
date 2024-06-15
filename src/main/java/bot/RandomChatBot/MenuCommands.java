package bot.RandomChatBot;

import bot.RandomChatBot.models.UserProperties;
import bot.RandomChatBot.service.UserService;
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
     */
    public FormCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && UserService.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        UserProperties properties = users.properties.get(user);
        SendMessage formMessage = SendMessage.builder()
                .chatId(user.getId())
                .text("=== Ваша анкета ===\n\n    Ваш пол: " + Gender.formatToRusString(properties.getGender()) + "  " + Emoji.GENDER +
                        "\n    Ваш возраст: " + properties.getAge() + " лет  " + Emoji.AGE +
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
        private static final InlineKeyboardButton GENDER_BUTTON = new InlineKeyboardButton("Изменить свой пол" + Emoji.GENDER);
        private static final InlineKeyboardButton MY_AGE_BUTTON = new InlineKeyboardButton("Изменить свой возраст" + Emoji.AGE);
        private static final List<List<InlineKeyboardButton>> BUTTONS;

        static {
            GENDER_BUTTON.setCallbackData(KeyboardData.SET_GENDER.getData());
            MY_AGE_BUTTON.setCallbackData(KeyboardData.SET_AGE.getData());
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
     */
    public FindSettingsCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && UserService.OVERRIDE.equals(strings[0]))) {
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
        return "=== Настройки поиска ===\n\n    Желаемый пол: " + Gender.formatToRusString(properties.getFindingGender()) + "  " + Emoji.FINDING_GENDER +
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
            FIND_GENDER_BUTTON.setCallbackData(KeyboardData.SET_FINDING_GENDER.getData());
            FIND_MIN_AGE_BUTTON.setCallbackData(KeyboardData.SET_MIN_FIND_AGE.getData());
            FIND_MAX_AGE_BUTTON.setCallbackData(KeyboardData.SET_MAX_FIND_AGE.getData());
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
                        "\n\n/start - Запустить бота" + Emoji.START +
                        "\n/form - Ваша анкета" + Emoji.FORM +
                        "\n/setage - Установить возраст" + Emoji.AGE +
                        "\n/setgender - Выбрать пол" + Emoji.GENDER +
                        "\n/setfindinggender - Указать желаемый пол" + Emoji.FINDING_GENDER +
                        "\n/setminfindingage - Указать мин. возраст поиска" + Emoji.MIN_FINDING_AGE +
                        "\n/setmaxfindingage - Указать макс. возраст поиска" + Emoji.MAX_FINDING_AGE +
                        "\n/random - Найти случайного собеседника" + Emoji.RANDOM +
                        "\n/stop - Остановить чат" + Emoji.STOP +
                        "\n/help - Список всех команд" + Emoji.HELP +
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
