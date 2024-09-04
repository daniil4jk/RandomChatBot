package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.FindingGenderKeyboard;
import ru.daniil4jk.randomChatBot.constants.Reports;

@Slf4j
@Component
public class SetFindingGenderCommand extends AbstractSetGenderCommand {
    @Autowired
    FindingGenderKeyboard genderKeyboard;

    public SetFindingGenderCommand() {
        super("setfindinggender",
                "Указать желаемый пол " + Emoji.FINDING_GENDER);
    }

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
    public void protectedExecute(@NotNull AbsSender absSender, long chatId, String[] strings) {
        if (getUserService().getUser(chatId).isPremium()) {
            super.protectedExecute(absSender, chatId, strings);
        } else {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.needPremium, chatId);
        }
    }

    @Override
    protected String getAskMessageText() {
        return "Выберите кого вы ищете\uD83D\uDE09, чтобы мы знали, кому вас подставлять в выдачу";
    }

    @Override
    protected InlineKeyboardMarkup getKeyboardToSend() {
        return genderKeyboard;
    }
}
