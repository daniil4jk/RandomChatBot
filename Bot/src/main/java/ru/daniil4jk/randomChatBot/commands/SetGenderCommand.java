package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.GenderKeyboard;

@Slf4j
@Component
public class SetGenderCommand extends AbstractSetGenderCommand {
    @Autowired
    GenderKeyboard genderKeyboard;

    public SetGenderCommand() {
        super("setgender", "Выбрать пол " + Emoji.GENDER);
    }

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

    @NotNull
    @Contract(pure = true)
    @Override
    protected String getAskMessageText() {
        return "Выберите кто вы\uD83D\uDE09, чтобы мы знали, кому вас подставлять в выдачу";
    }

    protected InlineKeyboardMarkup getKeyboardToSend() {
        return genderKeyboard;
    }
}

