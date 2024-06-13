package bot.RandomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
class SetGenderCommand extends UserInteractiveBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public SetGenderCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        SendMessage askMessage =
                SendMessage.builder().text(getMessageText())
                        .chatId(user.getId())
                        .replyMarkup(getKeyboard())
                        .build();
        try {
            absSender.execute(askMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    protected String getMessageText() {
        return "Выберите кто вы\uD83D\uDE09, чтобы мы знали, кому вас подставлять в выдачу";
    }

    protected InlineKeyboardMarkup getKeyboard() {
        return new MaleFemaleKeyboard();
    }

    static class MaleFemaleKeyboard extends InlineKeyboardMarkup {
        private static final InlineKeyboardButton MALE_BUTTON = new InlineKeyboardButton("Парень");
        private static final InlineKeyboardButton FEMALE_BUTTON = new InlineKeyboardButton("Девушка");
        private static final List<List<InlineKeyboardButton>> BUTTONS;
        static {
            MALE_BUTTON.setCallbackData(KeyboardData.SET_MALE_GENDER.getData());
            FEMALE_BUTTON.setCallbackData(KeyboardData.SET_FEMALE_GENDER.getData());
            BUTTONS = List.of(List.of(MALE_BUTTON, FEMALE_BUTTON));
        }

        public MaleFemaleKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

class SetFindingGenderCommand extends SetGenderCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public SetFindingGenderCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    protected String getMessageText() {
        return "Выберите, кого вы хотите искать?";
    }

    @Override
    protected InlineKeyboardMarkup getKeyboard() {
        return new MaleFemaleKeyboard();
    }

    static class MaleFemaleKeyboard extends InlineKeyboardMarkup {
        private static final InlineKeyboardButton MALE_BUTTON = new InlineKeyboardButton("Пареней");
        private static final InlineKeyboardButton FEMALE_BUTTON = new InlineKeyboardButton("Девушек");
        private static final List<List<InlineKeyboardButton>> BUTTONS;
        static {
            MALE_BUTTON.setCallbackData(KeyboardData.SET_MALE_FINDING_GENDER.getData());
            FEMALE_BUTTON.setCallbackData(KeyboardData.SET_FEMALE_FINDING_GENDER.getData());
            BUTTONS = List.of(List.of(MALE_BUTTON, FEMALE_BUTTON));
        }

        public MaleFemaleKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

@Slf4j
class SetAgeCommand extends UserInteractiveBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public SetAgeCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        SendMessage askMessage = SendMessage.builder()
                .chatId(user.getId())
                .text(getMessageText())
                .build();
        try {
            absSender.execute(askMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
        users.waitingMessageEvents.put(user, s -> {
            try {
                setAge(users.properties.get(user), Integer.parseInt(s));
                SendMessage successMessage = SendMessage.builder()
                        .chatId(user.getId())
                        .text("Вы успешно установили возраст" + Emoji.AGE)
                        .build();
                absSender.execute(successMessage);
            } catch (NumberFormatException e) {
                Reports.reportNotNumber(absSender, user);
            } catch (NullPointerException e) {
                Reports.reportEmptyAge(absSender, user);
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
        });
    }

    protected String getMessageText() {
        return "Введите ваш возраст\uD83D\uDE09";
    }

    protected void setAge(UserProperties properties, int age) {
        properties.setAge(age);
    }
}

class SetMinFindingAgeCommand extends SetAgeCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public SetMinFindingAgeCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    protected String getMessageText() {
        return "Введите минимальный возраст для поиска" + Emoji.MIN_FINDING_AGE;
    }

    protected void setAge(UserProperties properties, int age) {
        properties.setStartFindingAge(age);
    }
}

class SetMaxFindingAgeCommand extends SetAgeCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public SetMaxFindingAgeCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    protected String getMessageText() {
        return "Введите максимальный возраст для поиска" + Emoji.MAX_FINDING_AGE;
    }

    protected void setAge(UserProperties properties, int age) {
        properties.setEndRequiredAge(age);
    }
}
