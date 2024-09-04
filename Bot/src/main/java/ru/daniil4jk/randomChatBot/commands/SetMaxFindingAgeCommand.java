package ru.daniil4jk.randomChatBot.commands;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Reports;

@Component
public class SetMaxFindingAgeCommand extends AbstractSetAgeCommand {
    public SetMaxFindingAgeCommand() {
        super("setmaxfindingage", "Указать макс. возраст поиска " + Emoji.MAX_FINDING_AGE);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public SetMaxFindingAgeCommand(String commandIdentifier, String description) {
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
    protected void setAge(long chatId, int age) {
        getUserService().getUser(chatId).setMaxFindingAge(age);
    }

    @Override
    protected String getAskMessageText() {
        return "Введи максимальный вохраст, который хотите искать";
    }
}
