package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Keyboards.KeyboardData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class SetFindingGenderCommand extends SetGenderCommand {
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
            MALE_BUTTON.setCallbackData(KeyboardData.SET_BOY_FINDING_GENDER.getData());
            FEMALE_BUTTON.setCallbackData(KeyboardData.SET_GIRL_FINDING_GENDER.getData());
            BUTTONS = List.of(List.of(MALE_BUTTON, FEMALE_BUTTON));
        }

        public MaleFemaleKeyboard() {
            this.setKeyboard(BUTTONS);
        }
    }
}
