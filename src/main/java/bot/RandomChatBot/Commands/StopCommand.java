package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Keyboards.DefaultKeyboard;
import bot.RandomChatBot.Reports;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class StopCommand extends UserServiceIntegratedBotCommand {

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
        try {
            long UID = chat.getId();
            long secondUID = users.pairs.get(UID);
            writeDisconnectMessage(absSender, UID, secondUID);
            users.pairs.remove(UID);
            writeDisconnectMessage(absSender, secondUID, UID);
            users.pairs.remove(secondUID);
        } catch (NullPointerException e) {
            Reports.reportUnconnectedWriting(absSender, chat.getId(), user.getUserName());
        }
    }

    private void writeDisconnectMessage(AbsSender absSender, long UID, long secondUID) {
        SendMessage disconnectMessage = SendMessage.builder()
                .chatId(UID)
                .text("Вы успешно отсоединены от собеседника")
                .replyMarkup(new DefaultKeyboard())
                .build();
        log.trace("Пользователь " + users.getProperties(UID) + " отсоединен от " + users.getProperties(secondUID));
        try {
            absSender.execute(disconnectMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
