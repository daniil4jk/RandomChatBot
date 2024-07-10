package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Reports;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.NoSuchElementException;

@Slf4j
public class FindCommand extends ProtectedBotCommand {
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
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        try {
            if (strings.length > 1) throw new ArrayIndexOutOfBoundsException();
            long UID = user.getId();
            long findingUID = users.getPropertiesByUserName(strings[0]).getID();
            if (users.pairs.containsKey(UID) || users.pairs.containsKey(findingUID)) {
                throw new IllegalCallerException();
            }
            users.pairs.put(UID, findingUID);
            users.pairs.put(findingUID, UID);
            writeAboutConnection(absSender, UID, findingUID);
            writeAboutConnection(absSender, findingUID, UID);
        } catch (ArrayIndexOutOfBoundsException e) {
            Reports.reportIncorrectNickname(absSender, chat.getId(), null);
        } catch (NoSuchElementException e) {
            Reports.reportUnregisteredNickname(absSender, chat.getId(), strings[0]);
        } catch (IllegalCallerException e) {
            Reports.reportBusyUser(absSender, chat.getId(), strings[0]);
        }
    }

    private void writeAboutConnection(AbsSender absSender, long firstUID, long secondUID) {
        SendMessage errorMessage = SendMessage.builder()
                .chatId(firstUID)
                .text("Вы подключились к пользователю " + users.getProperties(secondUID).getUserName() + ")")
                .build();
        try {
            absSender.execute(errorMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}

