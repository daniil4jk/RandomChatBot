package ru.daniil4jk.randomChatBot.Commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.daniil4jk.randomChatBot.Reports;
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
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        if (getUsers().exist(chat.getId()) &&
                getUsers().getRCBUser(chat.getId()).isRegistred()
                ||
                strings != null && strings.length > 0 &&
                        UserService.OVERRIDE_USER_PASS.equals(strings[0])) {
            protectedExecute(absSender, user, chat, strings);
        } else {
            Reports.reportNeedRegistration(absSender, chat.getId());
        }
    }

    abstract public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings);
}
