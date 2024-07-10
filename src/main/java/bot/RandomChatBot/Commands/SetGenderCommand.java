package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Keyboards.KeyboardData;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class SetGenderCommand extends ProtectedBotCommand {
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
    public void protectedExecute(AbsSender absSender, org.telegram.telegrambots.meta.api.objects.User user, Chat chat, String[] strings) {
        SendMessage askMessage =
                SendMessage.builder().text(getMessageText())
                        .chatId(chat.getId())
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
            MALE_BUTTON.setCallbackData(KeyboardData.SET_BOY_GENDER.getData());
            FEMALE_BUTTON.setCallbackData(KeyboardData.SET_GIRL_GENDER.getData());
            BUTTONS = List.of(List.of(MALE_BUTTON, FEMALE_BUTTON));
        }

        public MaleFemaleKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}

