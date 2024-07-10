package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Reports;
import bot.RandomChatBot.service.UserService;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public abstract class ProtectedBotCommand extends UserServiceIntegratedBotCommand{
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
        if (!(users.exist(chat.getId()) && users.getProperties(chat.getId()).isRegistred() ||
                (strings != null && strings.length > 0 && UserService.OVERRIDE.equals(strings[0])))) {
            Reports.reportNeedRegistration(absSender, chat.getId());
            return;
        }
        protectedExecute(absSender, user, chat, strings);
    }

    abstract public void protectedExecute(AbsSender absSender, User user, Chat chat, String[] strings);
}
