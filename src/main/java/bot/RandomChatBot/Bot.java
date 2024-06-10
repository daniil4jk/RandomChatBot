package bot.RandomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class Bot extends TelegramLongPollingCommandBot {
    static BotConfig config;
    private final BotCommand startCommand = new StartCommand("start", "Запустить бота " + EmojiConstants.START);
    private final BotCommand setAgeCommand = new SetAgeCommand("setage", "Установить возраст " + EmojiConstants.AGE);
    private final BotCommand setGenderCommand = new SetGenderCommand("setgender", "Выбрать пол " + EmojiConstants.GENDER);
    private final BotCommand setFindingGenderCommand = new SetFindingGenderCommand("setfindinggender", "Указать желаемый пол " + EmojiConstants.FINDING_GENDER);
    private final BotCommand setMinFindingAgeCommand = new SetMinFindingAgeCommand("setminfindingage", "Указать мин. возраст поиска " + EmojiConstants.MIN_FINDING_AGE);
    private final BotCommand setMaxFindingAgeCommand = new SetMaxFindingAgeCommand("setmaxfindingage", "Указать макс. возраст поиска " + EmojiConstants.MAX_FINDING_AGE);
    private final BotCommand findCommand = new FindCommand("find", "Найти человека по никнейму)");
    private final BotCommand randomCommand = new FindRandomCommand("random", "Найти случайного собеседника " + EmojiConstants.RANDOM);
    private final BotCommand formCommand = new FormCommand("form", "Ваша анкета " + EmojiConstants.FORM);
    private final BotCommand findSettingsCommand = new FindSettingsCommand("findsettings", "Настройки поиска " + EmojiConstants.SETTINGS);
    private final BotCommand premiumCommand = new PremiumCommand("premium", "Платная подписка");
    private final BotCommand stopCommand = new StopCommand("stop", "Остановить чат " + EmojiConstants.STOP);
    private final BotCommand helpCommand = new HelpCommand("help", "Список всех команд " + EmojiConstants.HELP);

    public Bot(BotConfig config) {
        super(config.getToken());
        Bot.config = config;
        register(startCommand);
        register(setGenderCommand);
        register(setAgeCommand);
        register(setFindingGenderCommand);
        register(setMinFindingAgeCommand);
        register(setMaxFindingAgeCommand);
        register(findCommand);
        register(randomCommand);
        register(formCommand);
        register(findSettingsCommand);
        register(premiumCommand);
        register(stopCommand);
        register(helpCommand);
        sayAboutStart();
    }

    private void sayAboutStart() {
        SendMessage message = new SendMessage();
        message.setChatId(config.getAdminUID());
        message.setText("Бот успешно запустился!");
        try {
            execute(message);
        } catch (TelegramApiException ignored) {

        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (Users.waitingMessageEvents.containsKey(message.getFrom())) {
                processingMessageEvents(message);
            } else if (!processingMessageCommands(message)) {
                if (message.hasText() && Users.pairs.containsKey(message.getFrom())) {
                        copyMessage(update);
                } else if (message.hasSuccessfulPayment()) {
                    servePayment(message.getSuccessfulPayment(), message.getFrom());
                } else if (message.hasDice()) {
                    troll(message);
                } else {
                    Reports.reportUnconnectedWriting(this, message.getFrom());
                }
            }
            if (message.hasPhoto() || message.hasDocument() ||
                    message.hasVideo() || message.hasAudio() ||
                    message.hasVoice()) {
                copyFileToAdminCheck(message);
            }
        } else if (update.hasCallbackQuery()) {
            processingCallbackQuery(update.getCallbackQuery());
        } else if (update.hasPreCheckoutQuery()) {
            try {
                execute(new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true, "ВСЕ ОК"));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void copyMessage(Update update) {
        Users.messages.get(update.getMessage().getFrom()).add(update.getMessage());
        CopyMessage m = CopyMessage.builder()
                .fromChatId(update.getMessage().getChatId())
                .chatId(Users.pairs.get(update.getMessage().getFrom()).getId())
                .messageId(update.getMessage().getMessageId())
                .build();
        try {
            execute(m);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void copyFileToAdminCheck(Message message) {
        UserProperties userProperties = Users.properties.get(message.getFrom());
        String fileID = null;
        if (message.hasPhoto()) fileID = message.getPhoto().get(message.getPhoto().size() - 1).getFileId();
        else if (message.hasVideo()) fileID = message.getVideo().getFileId();
        else if (message.hasVoice()) fileID = message.getVoice().getFileId();
        else if (message.hasDocument()) fileID = message.getDocument().getFileId();
        else if (message.hasAudio()) fileID = message.getAudio().getFileId();
        SendMessage caption;
        CopyMessage pic;
        try {
            pic = CopyMessage.builder()
                    .fromChatId(message.getChatId())
                    .chatId(config.getMediaGroupId())
                    .messageId(message.getMessageId())
                    .caption("От: @" + message.getFrom().getUserName() +
                            "\nС UserID: " + message.getFrom().getId() +
                            "\nПола: " + Gender.formatToRusString(userProperties.getGender()) +
                            "\nВозраста: " + userProperties.getAge() +
                            "\nID файла: " + fileID)
                    .build();
        } catch (NullPointerException e) {
            return;
        }
        try {
            if (message.getCaption() != null) {
                caption = SendMessage.builder()
                        .text("@" + message.getFrom().getUserName() + ": " + message.getCaption())
                        .chatId(config.getMediaGroupId())
                        .build();
            } else {
                caption = null;
            }
        } catch (NullPointerException e) {
            caption = null;
        }
        try {
            if (caption != null) execute(caption);
            if (pic != null) execute(pic);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void troll(Message message) {
        try {
            execute(SendMessage.builder()
                    .text("Ты чаго, поприкалываться решил? Ну-ну, хвалю за изобретательность на кубике выпало " + message.getDice().getValue() + ")")
                    .chatId(message.getChatId())
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void servePayment(SuccessfulPayment payment, User user) {
        int days = Integer.parseInt(payment.getInvoicePayload());
        Users.properties.get(user).addPremium(Calendar.DATE, days);
    }

    private boolean processingMessageCommands(Message message) {
        return keyboardSwitch(message.getText(), message.getFrom());
    }

    private void processingCallbackQuery(CallbackQuery callback) {
        User user = Users.chatIDs.get(callback.getMessage().getChatId());
        if (user == null) return;
        keyboardSwitch(callback.getData(), user);
    }

    private boolean keyboardSwitch(String condition, User user) {
        switch (condition) {
            case KeyboardConstants.REGISTER_THREAD -> createRegThread(user);
            case KeyboardConstants.RANDOM -> randomCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.FORM -> formCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SETTINGS -> findSettingsCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.PREMIUM -> premiumCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.STOP -> stopCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_MALE_GENDER -> {
                Users.properties.get(user).setGender(Gender.Male);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_FEMALE_GENDER -> {
                Users.properties.get(user).setGender(Gender.Female);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_GENDER ->  setGenderCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_AGE -> setAgeCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_FINDING_GENDER ->  setFindingGenderCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_MALE_FINDING_GENDER -> {
                Users.properties.get(user).setFindingGender(Gender.Male);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_FEMALE_FINDING_GENDER -> {
                Users.properties.get(user).setFindingGender(Gender.Female);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_MIN_FIND_AGE -> setMinFindingAgeCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_MAX_FIND_AGE -> setMaxFindingAgeCommand
                    .execute(this, user, null, null);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void processingMessageEvents(Message userMessage) {
        User user = userMessage.getFrom();
        Users.waitingMessageEvents.get(user).accept(userMessage.getText());
        Users.waitingMessageEvents.remove(user);
    }

    private void createRegThread(User user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Users.properties.put(user, new UserProperties());
                UserProperties currentUserProperties = Users.properties.get(user);
                setGender(user, new String[]{Users.OVERRIDE});
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (currentUserProperties.isGenderNotStated());
                setAge(user, new String[]{Users.OVERRIDE});
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (Users.waitingMessageEvents.containsKey(user));
                Users.messages.put(user, new ArrayList<>());
                SendMessage successMessage = SendMessage.builder()
                        .chatId(user.getId())
                        .text("Вы успешно зарегистрированы, нажимайте /random и погнали чатиться\uD83E\uDD73)")
                        .replyMarkup(new DefaultKeyboard())
                        .build();
                try {
                    Bot.this.execute(successMessage);
                } catch (TelegramApiException e) {
                    log.warn("Не получилось отправить сообщение", e);
                }
            }

            private void setAge(User user, String[] text) {
                setAgeCommand.execute(Bot.this, user,
                        null, text);
            }

            private void setGender(User user, String[] text) {
                setGenderCommand.execute(Bot.this, user,
                        null, text);
            }

        }).start();
    }

    private void writeAboutSuccessGender(User user) {
        SendMessage successMessage = SendMessage.builder()
                .chatId(user.getId())
                .text("Вы успешно установили пол" + EmojiConstants.GENDER)
                .build();
        try {
            execute(successMessage);
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }
}

@Slf4j
class StartCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public StartCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        Users.chatIDs.put(chat.getId(), user);
        sendHelloMessage(absSender, user);
        reportRegistration(user);
    }

    private void sendHelloMessage(AbsSender absSender, User user) {
        SendPhoto firstMessage = SendPhoto.builder().chatId(user.getId())
                .caption("Привет, " + user.getFirstName() + "\uD83D\uDE0A ты попал(а) в самого лампового бота для анонимного общения в телеграмме")
                .photo(new InputFile("AgACAgIAAxkBAAIJk2Zjvk_Cf-4uznPnyMYMxwMpUe70AAKN2jEb_xUgS13GCak1cFjyAQADAgADeQADNQQ"))
                .build();
        SendMessage secondMessage = SendMessage.builder()
                .chatId(user.getId())
                .text("Перед тем как начать общение, необходимо указать информацию о себе, чтобы я понимал кого тебе подбирать\uD83E\uDDD0")
                .replyMarkup(new GoKey())
                .build();
        try {
            absSender.execute(firstMessage);
            Thread.sleep(200);
            absSender.execute(secondMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        } catch (InterruptedException e) {
            log.error("Поток прерван во время отправки приветственного сообщения", e);
        }
    }

    static class GoKey extends InlineKeyboardMarkup {
        private final InlineKeyboardButton GO_BUTTON = new InlineKeyboardButton("ПОЕЕЕХАЛИИИИ");
        private final List<List<InlineKeyboardButton>> BUTTONS;
        {
            GO_BUTTON.setCallbackData(KeyboardConstants.REGISTER_THREAD);
            BUTTONS = List.of(List.of(GO_BUTTON));
        }

        public GoKey() {
            this.setKeyboard(BUTTONS);
        }
    }

    private void reportRegistration(User user) {
        log.info("Зашел: @" + user.getUserName());
    }

    private void reportRegistrationToAdmin(AbsSender absSender, User user) {
        SendMessage errorMessage = SendMessage.builder()
                .chatId(Bot.config.getAdminUID())
                .text("Зашел user " + user.getUserName())
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }
}

@Slf4j
class SetGenderCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public SetGenderCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
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
            MALE_BUTTON.setCallbackData(KeyboardConstants.SET_MALE_GENDER);
            FEMALE_BUTTON.setCallbackData(KeyboardConstants.SET_FEMALE_GENDER);
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
     */
    public SetFindingGenderCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
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
            MALE_BUTTON.setCallbackData(KeyboardConstants.SET_MALE_FINDING_GENDER);
            FEMALE_BUTTON.setCallbackData(KeyboardConstants.SET_FEMALE_FINDING_GENDER);
            BUTTONS = List.of(List.of(MALE_BUTTON, FEMALE_BUTTON));
        }

        public MaleFemaleKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

@Slf4j
class SetAgeCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public SetAgeCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
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
        Users.waitingMessageEvents.put(user, s -> {
            try {
                setAge(Users.properties.get(user), Integer.parseInt(s));
                SendMessage successMessage = SendMessage.builder()
                        .chatId(user.getId())
                        .text("Вы успешно установили возраст" + EmojiConstants.AGE)
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
     */
    public SetMinFindingAgeCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    protected String getMessageText() {
        return "Введите минимальный возраст для поиска" + EmojiConstants.MIN_FINDING_AGE;
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
     */
    public SetMaxFindingAgeCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    protected String getMessageText() {
        return "Введите максимальный возраст для поиска" + EmojiConstants.MAX_FINDING_AGE;
    }

    protected void setAge(UserProperties properties, int age) {
        properties.setEndRequiredAge(age);
    }
}

@Slf4j
class FormCommand extends BotCommand {
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
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        UserProperties properties = Users.properties.get(user);
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
class FindSettingsCommand extends BotCommand {
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
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        if (Users.properties.get(user).isPremium()) {
            try {
                absSender.execute(SendMessage.builder()
                        .chatId(user.getId())
                        .text(getForm(Users.properties.get(user)))
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
class FindCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FindCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        try {
            if (strings.length > 1) throw new ArrayIndexOutOfBoundsException();
            User findingUser = findUserForName(strings[0]);
            if (findingUser == null) throw new NullPointerException();
            if (Users.pairs.containsKey(user) || Users.pairs.containsKey(findingUser)) {
                throw new IllegalCallerException();
            }
            Users.pairs.put(user, findingUser);
            Users.pairs.put(findingUser, user);
            writeAboutConnection(absSender, user, findingUser);
            writeAboutConnection(absSender, findingUser, user);
        } catch (ArrayIndexOutOfBoundsException e) {
            Reports.reportIncorrectNickname(absSender, user, null);
        } catch (NullPointerException e) {
            Reports.reportUnregisteredNickname(absSender, user, strings[0]);
        } catch (IllegalCallerException e) {
            Reports.reportBusyUser(absSender, user, strings[0]);
        }
    }

    private User findUserForName(String userName) {
        for (User u : Users.messages.keySet()) {
            if (userName.equals(u.getUserName())) {
                return u;
            }
        }
        return null;
    }

    private void writeAboutConnection(AbsSender absSender, User firstUser, User secondUser) {
        SendMessage errorMessage = SendMessage.builder()
                .chatId(firstUser.getId())
                .text("Вы подключились к пользователю " + secondUser.getFirstName() + ")")
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}

@Slf4j
class FindRandomCommand extends BotCommand {
    private static final int remainSeconds = 60;
    AbsSender sender;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FindRandomCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);

        new Thread(() -> {
            while (true) {
                if (Users.finders.size() > 1) {
                    sleep(1000/ Users.finders.size());
                    User firstUser = null;
                    User secondUser = null;
                    for (User u1 : Users.finders.keySet()) {
                        for (User u2 : Users.finders.keySet()) {
                            if (isGenderCompatible(u1, u2) && isAgeCompatible(u1, u2) && !u1.equals(u2)) {
                                firstUser = u1;
                                secondUser = u2;
                                break;
                            }
                        }
                    }
                    if (firstUser != null && secondUser != null) {
                        Users.finders.get(firstUser).cancel();
                        Users.finders.remove(firstUser);
                        Users.finders.get(secondUser).cancel();
                        Users.finders.remove(secondUser);
                        Users.pairs.put(firstUser, secondUser);
                        Users.pairs.put(secondUser, firstUser);
                        writeAboutConnection(sender, firstUser);
                        writeAboutConnection(sender, secondUser);
                    }
                } else {
                    sleep(1000);
                }
            }
        }).start();
    }

    private void sleep(long mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            log.error("Поток поиска собеседников прерван во время ожидания!", e);
        }
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!absSender.equals(sender)) sender = absSender;
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        if (Users.finders.containsKey(user)) {
            Reports.reportWaiting(absSender, user);
            return;
        }
        Users.finders.put(user, new Timer());
        Users.finders.get(user).schedule(new removeUserTask(user),
                remainSeconds * 1000);
        writeAboutSearching(absSender, user);
    }

    class removeUserTask extends TimerTask {
        User userToRemove;
        public removeUserTask(User userToRemove) {
            this.userToRemove = userToRemove;
        }

        @Override
        public void run() {
            synchronized (Users.finders) {
                Users.finders.remove(userToRemove);
                writeAboutRemove(sender, userToRemove);
            }
        }
    }

    private void writeAboutSearching(AbsSender absSender, User user) {
        SendMessage searchMessage = SendMessage.builder()
                .chatId(user.getId())
                .text("Сейчас тебе кого-нибудь подыщем☺\uFE0F")
                .build();
        try {
            absSender.execute(searchMessage);
            int sendDiceValue = absSender.execute(SendDice.builder().chatId(user.getId()).build()).getDice().getValue();
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutRemove(AbsSender absSender, User user) {
        SendMessage errorMessage = SendMessage.builder()
                .chatId(user.getId())
                .text("Извините, " + (Users.properties.get(user).isPremium() ? "по вашим параметрам" : "для вас") +
                        " никого найти не удалось\uD83D\uDE14, но не расстраивайся, солнышко, ты можешь попробовать еще раз, мы верим в тебя!")
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private boolean isGenderCompatible(User firstUser, User secondUser) {
        HashMap<User, UserProperties> props = Users.properties;
        if (!props.get(firstUser).isPremium() && !props.get(secondUser).isPremium()) {
            return true;
        }
        boolean isFirstUserSuitableForSecondUser = Gender.equals(
                props.get(secondUser).getFindingGender(),
                        props.get(firstUser).getGender());
        boolean isSecondUserSuitableForFirstUser = Gender.equals(
                props.get(firstUser).getFindingGender(),
                        props.get(secondUser).getGender());
        return isFirstUserSuitableForSecondUser || !props.get(secondUser).isPremium() &&
                isSecondUserSuitableForFirstUser || !props.get(firstUser).isPremium();
    }

    private boolean isAgeCompatible(User firstUser, User secondUser) {
        HashMap<User, UserProperties> props = Users.properties;
        if (!props.get(firstUser).isPremium() && !props.get(secondUser).isPremium()) {
            return true;
        }
        boolean isFirstUserSuitable =
                props.get(secondUser).getStartFindingAge() <
                props.get(firstUser).getAge() &&
                        props.get(firstUser).getAge() <
                                props.get(secondUser).getEndRequiredAge();
        boolean isSecondUserSuitable =
                props.get(firstUser).getStartFindingAge() <
                props.get(secondUser).getAge() &&
                        props.get(secondUser).getAge() <
                                props.get(firstUser).getEndRequiredAge();
        return isFirstUserSuitable | !props.get(secondUser).isPremium() &&
                isSecondUserSuitable | !props.get(firstUser).isPremium();
    }

    protected void writeAboutConnection(AbsSender absSender, User user) {
        try {
            absSender.execute(SendMessage.builder().chatId(user.getId())
                    .text("Я нашел тебе собеседника! Приятного знакомства)").build());
            if (UserProperties.isPremiumSystemActive()) {
                absSender.execute(SendMessage.builder()
                        .chatId(user.getId())
                        .text(getForm(Users.properties.get(user).isPremium(), Users.properties.get(Users.pairs.get(user))))
                        .replyMarkup(new ChatKeyboard()).build());
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private String getForm(boolean canWatch, UserProperties properties) {
        return "=== Анкета пользователя ===" +
                "\n\n    Пол собеседника: " + (canWatch ? Gender.formatToRusString(properties.getGender()) : " ----")+
                "\n    Возраст собеседника: " + (canWatch ? (properties.getAge() == 0 ? "Не указано" : String.valueOf(properties.getAge())) : " ----") +
                (canWatch ? "\n" : "\n\nЧтобы разблокировать просмотр пола и возраста собеседника нужно стать Premium" + EmojiConstants.PREMIUM) +
                "\n===========================";
    }
}

@Slf4j
class StopCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public StopCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        try {
            User secondUser = Users.pairs.get(user);
            writeDisconnectMessage(absSender, user, secondUser);
            Users.pairs.remove(user);
            writeDisconnectMessage(absSender, secondUser, user);
            Users.pairs.remove(secondUser);
        } catch (NullPointerException e) {
            Reports.reportUnconnectedWriting(absSender, user);
        }
    }

    private void writeDisconnectMessage(AbsSender absSender, User firstUser, User secondUser) {
        SendMessage disconnectMessage = SendMessage.builder()
                .chatId(firstUser.getId())
                .text("Вы успешно отсоединены от собеседника")
                .replyMarkup(new DefaultKeyboard())
                .build();
        log.trace("Пользователь " + firstUser.getUserName() + " отсоединен от " + secondUser);
        try {
            absSender.execute(disconnectMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}

class PremiumCommand extends BotCommand {

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public PremiumCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(Users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && Users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Купить премиум подписку\nПодписка подключается моментально после покупки")
                    .replyMarkup(new PremiumKeyboard(absSender))
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    static class PremiumKeyboard extends InlineKeyboardMarkup {
        private final InlineKeyboardButton PAY_DAY_BUTTON = new InlineKeyboardButton("Купить на день");
        private final InlineKeyboardButton PAY_WEEK_BUTTON = new InlineKeyboardButton("Купить на неделю");
        private final InlineKeyboardButton PAY_MOUNTH_BUTTON = new InlineKeyboardButton("Купить на месяц");
        private final List<List<InlineKeyboardButton>> BUTTONS;

        {
            BUTTONS = List.of(
                    List.of(PAY_DAY_BUTTON),
                    List.of(PAY_WEEK_BUTTON),
                    List.of(PAY_MOUNTH_BUTTON)
            );
        }

        public PremiumKeyboard(AbsSender absSender) {
            try {
                PAY_DAY_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                        .title("Премиум 1 день")
                        .description("Все возможности премиум подписки на 1 день")
                        .payload("1")
                        .providerToken("381764678:TEST:87212")
                        .currency("RUB")
                        .price(new LabeledPrice("Цена премиума на ДЕНЬ (мин. возможная цена в тг!)", 7000))
                        .build()));
                PAY_WEEK_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                        .title("Премиум 1 неделя")
                        .description("Все возможности премиум подписки на 1 неделю")
                        .payload("7")
                        .providerToken("381764678:TEST:87212")
                        .currency("RUB")
                        .price(new LabeledPrice("Цена премиума на 7 ДНЕЙ", 20000))
                        .build()));
                PAY_MOUNTH_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                        .title("Премиум 1 месяц")
                        .description("Все возможности премиум подписки на 1 месяц")
                        .payload("31")
                        .providerToken("381764678:TEST:87212")
                        .currency("RUB")
                        .price(new LabeledPrice("Цена премиума на 31 ДЕНЬ", 50000))
                        .build()));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
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

class DefaultKeyboard extends ReplyKeyboardMarkup {
    private static final List<KeyboardRow> keyboard = new ArrayList<>();
    private static final KeyboardRow firstRow = new KeyboardRow();
    private static final KeyboardButton randomButton = new KeyboardButton(KeyboardConstants.RANDOM);
    private static final KeyboardRow secondRow = new KeyboardRow();
    private static final KeyboardButton formButton = new KeyboardButton(KeyboardConstants.FORM);
    private static final KeyboardButton settingsButton = new KeyboardButton(KeyboardConstants.SETTINGS);
    private static final KeyboardRow thirdRow = new KeyboardRow();
    private static final KeyboardButton premiumButton = new KeyboardButton(KeyboardConstants.PREMIUM);

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
    private static final KeyboardButton stopButton = new KeyboardButton(KeyboardConstants.STOP);

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