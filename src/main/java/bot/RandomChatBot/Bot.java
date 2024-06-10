package bot.RandomChatBot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingCommandBot {
    private final BotConfig config;
    private final Users users;
    private final BotCommand startCommand;
    private final BotCommand setAgeCommand;
    private final BotCommand setGenderCommand;
    private final BotCommand setFindingGenderCommand;
    private final BotCommand setMinFindingAgeCommand;
    private final BotCommand setMaxFindingAgeCommand;
    private final BotCommand findCommand;
    private final BotCommand randomCommand;
    private final BotCommand formCommand;
    private final BotCommand findSettingsCommand;
    private final BotCommand premiumCommand;
    private final BotCommand stopCommand;
    private final BotCommand helpCommand;

    public Bot(BotConfig config, Users users) {
        super(config.getToken());
        this.config = config;
        this.users = users;
        startCommand = new StartCommand("start", "Запустить бота " + EmojiConstants.START, this.users, this.config);
        register(startCommand);
        setGenderCommand = new SetGenderCommand("setgender", "Выбрать пол " + EmojiConstants.GENDER, this.users);
        register(setGenderCommand);
        setAgeCommand = new SetAgeCommand("setage", "Установить возраст " + EmojiConstants.AGE, this.users);
        register(setAgeCommand);
        setFindingGenderCommand = new SetFindingGenderCommand("setfindinggender", "Указать желаемый пол " + EmojiConstants.FINDING_GENDER, this.users);
        register(setFindingGenderCommand);
        setMinFindingAgeCommand = new SetMinFindingAgeCommand("setminfindingage", "Указать мин. возраст поиска " + EmojiConstants.MIN_FINDING_AGE, this.users);
        register(setMinFindingAgeCommand);
        setMaxFindingAgeCommand = new SetMaxFindingAgeCommand("setmaxfindingage", "Указать макс. возраст поиска " + EmojiConstants.MAX_FINDING_AGE, this.users);
        register(setMaxFindingAgeCommand);
        findCommand = new FindCommand("find", "Найти человека по никнейму)", this.users);
        register(findCommand);
        randomCommand = new FindRandomCommand("random", "Найти случайного собеседника " + EmojiConstants.RANDOM, this.users);
        register(randomCommand);
        formCommand = new FormCommand("form", "Ваша анкета " + EmojiConstants.FORM, this.users);
        register(formCommand);
        findSettingsCommand = new FindSettingsCommand("findsettings", "Настройки поиска " + EmojiConstants.SETTINGS, this.users);
        register(findSettingsCommand);
        premiumCommand = new PremiumCommand("premium", "Платная подписка", this.users);
        register(premiumCommand);
        stopCommand = new StopCommand("stop", "Остановить чат " + EmojiConstants.STOP, this.users);
        register(stopCommand);
        helpCommand = new HelpCommand("help", "Список всех команд " + EmojiConstants.HELP);
        register(helpCommand);
        sayAboutStart();
    }

    private void sayAboutStart() {
        SendMessage message = new SendMessage();
        message.setChatId(config.getAdminUID());
        message.setText("Бот успешно запустился!");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
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
            if (users.waitingMessageEvents.containsKey(message.getFrom())) {
                processingMessageEvents(message);
            } else if (!processingMessageCommands(message)) {
                if (message.hasText() && users.pairs.containsKey(message.getFrom())) {
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
                log.warn("Не получилось отправить сообщение", e);
            }
        }
    }

    private void copyMessage(Update update) {
        users.messages.get(update.getMessage().getFrom()).add(update.getMessage());
        CopyMessage m = CopyMessage.builder()
                .fromChatId(update.getMessage().getChatId())
                .chatId(users.pairs.get(update.getMessage().getFrom()).getId())
                .messageId(update.getMessage().getMessageId())
                .build();
        try {
            execute(m);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение между пользователями", e);
        }
    }

    private void copyFileToAdminCheck(Message message) {
        UserProperties userProperties = users.properties.get(message.getFrom());
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
        users.properties.get(user).addPremium(Calendar.DATE, days);
    }

    private boolean processingMessageCommands(Message message) {
        return keyboardSwitch(message.getText(), message.getFrom());
    }

    private void processingCallbackQuery(CallbackQuery callback) {
        User user = users.chatIDs.get(callback.getMessage().getChatId());
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
                users.properties.get(user).setGender(Gender.Male);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_FEMALE_GENDER -> {
                users.properties.get(user).setGender(Gender.Female);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_GENDER ->  setGenderCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_AGE -> setAgeCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_FINDING_GENDER ->  setFindingGenderCommand
                    .execute(this, user, null, null);
            case KeyboardConstants.SET_MALE_FINDING_GENDER -> {
                users.properties.get(user).setFindingGender(Gender.Male);
                writeAboutSuccessGender(user);
            }
            case KeyboardConstants.SET_FEMALE_FINDING_GENDER -> {
                users.properties.get(user).setFindingGender(Gender.Female);
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
        users.waitingMessageEvents.get(user).accept(userMessage.getText());
        users.waitingMessageEvents.remove(user);
    }

    private void createRegThread(User user) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                users.properties.put(user, new UserProperties());
                UserProperties currentUserProperties = users.properties.get(user);
                setGender(user, new String[]{users.OVERRIDE});
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (currentUserProperties.isGenderNotStated());
                setAge(user, new String[]{users.OVERRIDE});
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } while (users.waitingMessageEvents.containsKey(user));
                users.messages.put(user, new ArrayList<>());
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
class StartCommand extends UserInteractiveBotCommand {
    private final BotConfig config;
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public StartCommand(String commandIdentifier, String description, Users users, BotConfig config) {
        super(commandIdentifier, description, users);
        this.config = config;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        users.chatIDs.put(chat.getId(), user);
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
                .chatId(config.getAdminUID())
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
class StopCommand extends UserInteractiveBotCommand {

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public StopCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        try {
            User secondUser = users.pairs.get(user);
            writeDisconnectMessage(absSender, user, secondUser);
            users.pairs.remove(user);
            writeDisconnectMessage(absSender, secondUser, user);
            users.pairs.remove(secondUser);
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

class PremiumCommand extends UserInteractiveBotCommand {

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users 			users storage
     */
    public PremiumCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
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
