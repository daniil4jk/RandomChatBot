package ru.daniil4jk.randomChatBot.commands;

import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.service.UserService;

public abstract class ProtectedBotCommand extends UserServiceIntegratedBotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public ProtectedBotCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, long chatId, String[] strings) {
        if (getUserService().contains(chatId) &&
                getUserService().getUser(chatId).isRegistred()
                ||
                strings != null && strings.length > 0 &&
                        UserService.OVERRIDE_USER_PASS.equals(strings[0])) {
            protectedExecute(absSender, chatId, strings);
        } else {
            ((AbstractBot) absSender).sendSimpleTextMessage(Reports.needRegistration, chatId);
        }
    }

    abstract public void protectedExecute(AbsSender absSender, long chatId, String[] strings);
}
