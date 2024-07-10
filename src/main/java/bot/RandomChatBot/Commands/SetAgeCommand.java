package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Constants.Emoji;
import bot.RandomChatBot.Keyboards.DefaultKeyboard;
import bot.RandomChatBot.Reports;
import bot.RandomChatBot.models.UserProperties;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class SetAgeCommand extends ProtectedBotCommand {
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
    public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings) {
        SendMessage askMessage = SendMessage.builder()
                .chatId(chat.getId())
                .text(getMessageText())
                .build();
        try {
            absSender.execute(askMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
        users.waitingMessageEvents.put(chat.getId(), s -> {
            try {
                setAge(users.getProperties(chat.getId()), Integer.parseInt(s.getText()));
                SendMessage successMessage = SendMessage.builder()
                        .chatId(chat.getId())
                        .text("Вы успешно установили возраст" + Emoji.AGE)
                        .build();
                absSender.execute(successMessage);
            } catch (NumberFormatException e) {
                Reports.reportNotNumber(absSender, chat.getId());
            } catch (NullPointerException e) {
                Reports.reportEmptyAge(absSender, chat.getId());
            } catch (TelegramApiException e) {
                log.warn("Не получилось отправить сообщение", e);
            }
            if (!users.getProperties(chat.getId()).isRegistred()) {
                users.getProperties(chat.getId()).setRegistred(true);
                users.getProperties(chat.getId()).setUserName(s.getFrom().getUserName());
                writeAboutSuccessfullRegistration(absSender, chat.getId());
            }
        });
    }

    private void writeAboutSuccessfullRegistration(AbsSender absSender, long chatID) {
        SendMessage successMessage = SendMessage.builder()
                .chatId(chatID)
                .text("Вы успешно зарегистрированы, нажимайте /random и погнали чатиться\uD83E\uDD73)")
                .replyMarkup(new DefaultKeyboard())
                .build();
        try {
            absSender.execute(successMessage);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }

    protected String getMessageText() {
        return "Введите ваш возраст\uD83D\uDE09";
    }

    protected UserProperties setAge(UserProperties properties, int age) {
        properties.setAge(age);
        return properties;
    }
}
