package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Constants.Emoji;
import bot.RandomChatBot.Constants.Gender;
import bot.RandomChatBot.Keyboards.ChatKeyboard;
import bot.RandomChatBot.Reports;
import bot.RandomChatBot.models.UserProperties;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class FindRandomCommand extends ProtectedBotCommand {
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
                if (users.finders.size() > 1) {
                    sleep(1000 / users.finders.size());
                    Long firstUser = null;
                    Long secondUser = null;
                    for (long u1 : users.finders.keySet()) {
                        for (long u2 : users.finders.keySet()) {
                            if (isGenderTwoSidedCompatible(users.getProperties(u1), users.getProperties(u2)) &&
                                    isAgeCompatible(users.getProperties(u1), users.getProperties(u2)) &&
                                    u1 != u2) {
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
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (!absSender.equals(sender)) sender = absSender;
        if (users.finders.containsKey(user.getId())) {
            Reports.reportWaiting(absSender, chat.getId());
            return;
        }
        users.finders.put(user.getId(), new Timer());
        users.finders.get(user.getId()).schedule(new removeUserTask(user),
                remainSeconds * 1000);
        writeAboutSearching(absSender, chat.getId());
    }

    private void writeAboutSearching(AbsSender absSender, long chatID) {
        SendMessage searchMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Сейчас тебе кого-нибудь подыщем☺\uFE0F")
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
                .text("Извините, " + (users.getProperties(UID).isPremium() ? "по вашим параметрам" : "для вас") +
                        " никого найти не удалось\uD83D\uDE14, но не расстраивайся, солнышко, ты можешь попробовать еще раз, мы верим в тебя!")
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private boolean isGenderTwoSidedCompatible(UserProperties firstUserProperties, UserProperties secondUserProperties) {

        if (!firstUserProperties.isPremium() && !secondUserProperties.isPremium()) {
            return true;
        }

        boolean isSecondUserCompatibleForFirstUser = isGenderCompatible(
                firstUserProperties.getFindingGender(),
                secondUserProperties.getGender());
        boolean isFirstUserCompatibleForSecondUser = isGenderCompatible(
                secondUserProperties.getFindingGender(),
                firstUserProperties.getGender());

        return isFirstUserCompatibleForSecondUser || !secondUserProperties.isPremium() &&
                isSecondUserCompatibleForFirstUser || !firstUserProperties.isPremium();
    }

    private boolean isGenderCompatible(Gender findingGender, Gender gender) {
        if (findingGender.equals(Gender.NotStated)) {
            return true;
        }
        return Objects.equals(findingGender, gender);
    }

    private boolean isAgeCompatible(UserProperties firstUserProperties, UserProperties secondUserProperties) {
        if (!firstUserProperties.isPremium() && !secondUserProperties.isPremium()) {
            return true;
        }

        boolean isFirstUserSuitable =
                secondUserProperties.getStartFindingAge() <
                        firstUserProperties.getAge() &&
                        firstUserProperties.getAge() <
                                secondUserProperties.getEndRequiredAge();
        boolean isSecondUserSuitable =
                firstUserProperties.getStartFindingAge() <
                       secondUserProperties.getAge() &&
                        secondUserProperties.getAge() <
                                firstUserProperties.getEndRequiredAge();

        return isFirstUserSuitable | !secondUserProperties.isPremium() &&
                isSecondUserSuitable | !firstUserProperties.isPremium();
    }

    protected void writeAboutConnection(AbsSender absSender, long UID) {
        try {
            absSender.execute(SendMessage.builder().chatId(UID)
                    .text("Я нашел тебе собеседника! Приятного знакомства)").build());
            if (UserProperties.premiumSystemActive) {
                absSender.execute(SendMessage.builder()
                        .chatId(UID)
                        .text(getForm(users.getProperties(UID).isPremium(), users.getProperties(users.pairs.get(UID))))
                        .replyMarkup(new ChatKeyboard()).build());
            }
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    private String getForm(boolean canWatch, UserProperties properties) {
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
            synchronized (users.finders) {
                users.finders.remove(userToRemove.getId());
                writeAboutRemove(sender, userToRemove.getId());
            }
        }
    }
}
