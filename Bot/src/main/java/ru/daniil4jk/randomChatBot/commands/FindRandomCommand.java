package ru.daniil4jk.randomChatBot.commands;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.keyboards.ChatKeyboard;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;

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
                if (getUserService().finders.size() > 1) {
                    sleep(1000 / getUserService().finders.size());
                    Long firstUser = null;
                    Long secondUser = null;
                    for (long u1 : getUserService().finders.keySet()) {
                        for (long u2 : getUserService().finders.keySet()) {
                            if (isGenderTwoSidedCompatible(getUserService().getUser(u1), getUserService().getUser(u2)) &&
                                    isAgeCompatible(getUserService().getUser(u1), getUserService().getUser(u2)) &&
                                    u1 != u2) {
                                firstUser = u1;
                                secondUser = u2;
                                break;
                            }
                        }
                    }
                    if (firstUser != null && secondUser != null) {
                        getUserService().finders.get(firstUser).cancel();
                        getUserService().finders.remove(firstUser);
                        getUserService().finders.get(secondUser).cancel();
                        getUserService().finders.remove(secondUser);
                        getUserService().pairs.put(firstUser, secondUser);
                        getUserService().pairs.put(secondUser, firstUser);
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
    public void protectedExecute(AbsSender absSender, long chatId, String[] strings) {
        if (!absSender.equals(sender)) sender = absSender;
        if (getUserService().finders.containsKey(chatId)) {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.waiting, chatId);
            return;
        }
        getUserService().finders.put(chatId, new Timer());
        getUserService().finders.get(chatId).schedule(new removeUserTask(chatId),
                remainSeconds * 1000);
        writeAboutSearching(absSender, chatId);
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
                .text("Извини, " + (getUserService().getUser(UID).isPremium() ? "по вашим параметрам" : "для вас") +
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
                secondRandomChatBotUser.getMinFindingAge() <
                        firstRandomChatBotUser.getAge() &&
                        firstRandomChatBotUser.getAge() <
                                secondRandomChatBotUser.getMaxFindingAge();
        boolean isSecondUserSuitable =
                firstRandomChatBotUser.getMinFindingAge() <
                        secondRandomChatBotUser.getAge() &&
                        secondRandomChatBotUser.getAge() <
                                firstRandomChatBotUser.getMaxFindingAge();

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
                        .text(getForm(getUserService().getUser(UID).isPremium(),
                                getUserService().getUser(getUserService().pairs.get(UID))))
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
        long userIdToRemove;

        public removeUserTask(long userToRemove) {
            this.userIdToRemove = userToRemove;
        }

        @Override
        public void run() {
            synchronized (getUserService().finders) {
                getUserService().finders.remove(userIdToRemove);
                writeAboutRemove(sender, userIdToRemove);
            }
        }
    }
}
