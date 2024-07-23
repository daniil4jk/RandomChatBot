package ru.daniil4jk.randomChatBot.Commands;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.Reports;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.keyboards.ChatKeyboard;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.service.BotConfig;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Component
public class FindRandomCommand extends ProtectedBotCommand {
    private static final int remainSeconds = 60;
    AbsSender sender;
    @Autowired
    BotConfig config;
    @Autowired
    ChatKeyboard chatKeyboard;

    public FindRandomCommand() {
        super("random", "Найти случайного собеседника " + Emoji.RANDOM);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public FindRandomCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @PostConstruct
    private void createFindingThread() {
        new Thread(() -> {
            while (true) {
                if (getUsers().finders.size() > 1) {
                    sleep(1000 / getUsers().finders.size());
                    Long firstUser = null;
                    Long secondUser = null;
                    for (long u1 : getUsers().finders.keySet()) {
                        for (long u2 : getUsers().finders.keySet()) {
                            if (isGenderTwoSidedCompatible(getUsers().getProperties(u1), getUsers().getProperties(u2)) &&
                                    isAgeCompatible(getUsers().getProperties(u1), getUsers().getProperties(u2)) &&
                                    u1 != u2) {
                                firstUser = u1;
                                secondUser = u2;
                                break;
                            }
                        }
                    }
                    if (firstUser != null && secondUser != null) {
                        getUsers().finders.get(firstUser).cancel();
                        getUsers().finders.remove(firstUser);
                        getUsers().finders.get(secondUser).cancel();
                        getUsers().finders.remove(secondUser);
                        getUsers().pairs.put(firstUser, secondUser);
                        getUsers().pairs.put(secondUser, firstUser);
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
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!absSender.equals(sender)) sender = absSender;
        if (getUsers().finders.containsKey(user.getId())) {
            Reports.reportWaiting(absSender, chat.getId());
            return;
        }
        getUsers().finders.put(user.getId(), new Timer());
        getUsers().finders.get(user.getId()).schedule(new removeUserTask(user),
                remainSeconds * 1000);
        writeAboutSearching(absSender, chat.getId());
    }

    private void writeAboutSearching(AbsSender absSender, long chatID) {
        SendMessage searchMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Сейчас тебе кого-нибудь подыщем " + Emoji.CUTE_CLOSINGEYES)
                .build();
        try {
            absSender.execute(searchMessage);
            int sendDiceValue = absSender.execute(SendDice.builder().chatId(chatID).build()).getDice().getValue();
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private void writeAboutRemove(AbsSender absSender, long UID) {
        SendMessage errorMessage = SendMessage.builder()
                .chatId(UID)
                .text("Извините, " + (getUsers().getProperties(UID).isPremium() ? "по вашим параметрам" : "для вас") +
                        " никого найти не удалось\uD83D\uDE14, но не расстраивайся, солнышко, ты можешь попробовать еще раз, мы верим в тебя!")
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private boolean isGenderTwoSidedCompatible(RandomChatBotUser firstRandomChatBotUser, RandomChatBotUser secondRandomChatBotUser) {

        if (!firstRandomChatBotUser.isPremium() && !secondRandomChatBotUser.isPremium()) {
            return true;
        }

        boolean isSecondUserCompatibleForFirstUser = isGenderCompatible(
                firstRandomChatBotUser.getFindingGender(),
                secondRandomChatBotUser.getGender());
        boolean isFirstUserCompatibleForSecondUser = isGenderCompatible(
                secondRandomChatBotUser.getFindingGender(),
                firstRandomChatBotUser.getGender());

        return isFirstUserCompatibleForSecondUser || !secondRandomChatBotUser.isPremium() &&
                isSecondUserCompatibleForFirstUser || !firstRandomChatBotUser.isPremium();
    }

    private boolean isGenderCompatible(Gender findingGender, Gender gender) {
        if (findingGender.equals(Gender.NotStated)) {
            return true;
        }
        return Objects.equals(findingGender, gender);
    }

    private boolean isAgeCompatible(RandomChatBotUser firstRandomChatBotUser, RandomChatBotUser secondRandomChatBotUser) {
        if (!firstRandomChatBotUser.isPremium() && !secondRandomChatBotUser.isPremium()) {
            return true;
        }

        boolean isFirstUserSuitable =
                secondRandomChatBotUser.getStartFindingAge() <
                        firstRandomChatBotUser.getAge() &&
                        firstRandomChatBotUser.getAge() <
                                secondRandomChatBotUser.getEndRequiredAge();
        boolean isSecondUserSuitable =
                firstRandomChatBotUser.getStartFindingAge() <
                        secondRandomChatBotUser.getAge() &&
                        secondRandomChatBotUser.getAge() <
                                firstRandomChatBotUser.getEndRequiredAge();

        return isFirstUserSuitable | !secondRandomChatBotUser.isPremium() &&
                isSecondUserSuitable | !firstRandomChatBotUser.isPremium();
    }

    protected void writeAboutConnection(AbsSender absSender, long UID) {
        try {
            absSender.execute(SendMessage.builder().chatId(UID)
                    .text("Я нашел тебе собеседника! Приятного знакомства)").build());
            if (config.isPremiumSystemActive()) {
                absSender.execute(SendMessage.builder()
                        .chatId(UID)
                        .text(getForm(getUsers().getProperties(UID).isPremium(),
                                getUsers().getProperties(getUsers().pairs.get(UID))))
                        .replyMarkup(chatKeyboard)
                        .build());
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private String getForm(boolean canWatch, RandomChatBotUser properties) {
        return "=== Анкета пользователя ===" +
                "\n\n    Пол собеседника: " + (canWatch ? properties.getGender().toRusString() : " ----") +
                "\n    Возраст собеседника: " + (canWatch ? (properties.getAge() == 0 ? "Не указано" : String.valueOf(properties.getAge())) : " ----") +
                (canWatch ? "\n" : "\n\nЧтобы разблокировать просмотр пола и возраста собеседника нужно стать Premium" + Emoji.PREMIUM) +
                "\n===========================";
    }

    class removeUserTask extends TimerTask {
        User userToRemove;

        public removeUserTask(User userToRemove) {
            this.userToRemove = userToRemove;
        }

        @Override
        public void run() {
            synchronized (getUsers().finders) {
                getUsers().finders.remove(userToRemove.getId());
                writeAboutRemove(sender, userToRemove.getId());
            }
        }
    }
}
