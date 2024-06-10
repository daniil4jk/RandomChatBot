package bot.RandomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
class FindCommand extends UserInteractiveBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public FindCommand(String commandIdentifier, String description, Users users) {
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
            if (strings.length > 1) throw new ArrayIndexOutOfBoundsException();
            User findingUser = findUserForName(strings[0]);
            if (findingUser == null) throw new NullPointerException();
            if (users.pairs.containsKey(user) || users.pairs.containsKey(findingUser)) {
                throw new IllegalCallerException();
            }
            users.pairs.put(user, findingUser);
            users.pairs.put(findingUser, user);
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
        for (User u : users.messages.keySet()) {
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
class FindRandomCommand extends UserInteractiveBotCommand {
    private static final int remainSeconds = 60;
    AbsSender sender;
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public FindRandomCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description, users);

        new Thread(() -> {
            while (true) {
                if (users.finders.size() > 1) {
                    sleep(1000/ users.finders.size());
                    User firstUser = null;
                    User secondUser = null;
                    for (User u1 : users.finders.keySet()) {
                        for (User u2 : users.finders.keySet()) {
                            if (isGenderCompatible(u1, u2) && isAgeCompatible(u1, u2) && !u1.equals(u2)) {
                                firstUser = u1;
                                secondUser = u2;
                                break;
                            }
                        }
                    }
                    if (firstUser != null && secondUser != null) {
                        users.finders.get(firstUser).cancel();
                        users.finders.remove(firstUser);
                        users.finders.get(secondUser).cancel();
                        users.finders.remove(secondUser);
                        users.pairs.put(firstUser, secondUser);
                        users.pairs.put(secondUser, firstUser);
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
        if (!(users.messages.containsKey(user) || strings != null &&
                strings.length > 0 && users.OVERRIDE.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, user.getId());
            return;
        }
        if (users.finders.containsKey(user)) {
            Reports.reportWaiting(absSender, user);
            return;
        }
        users.finders.put(user, new Timer());
        users.finders.get(user).schedule(new removeUserTask(user),
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
            synchronized (users.finders) {
                users.finders.remove(userToRemove);
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
                .text("Извините, " + (users.properties.get(user).isPremium() ? "по вашим параметрам" : "для вас") +
                        " никого найти не удалось\uD83D\uDE14, но не расстраивайся, солнышко, ты можешь попробовать еще раз, мы верим в тебя!")
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private boolean isGenderCompatible(User firstUser, User secondUser) {
        HashMap<User, UserProperties> props = users.properties;
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
        HashMap<User, UserProperties> props = users.properties;
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
                        .text(getForm(users.properties.get(user).isPremium(), users.properties.get(users.pairs.get(user))))
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
