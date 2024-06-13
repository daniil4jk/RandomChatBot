package bot.RandomChatBot;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;

public abstract class UserInteractiveBotCommand extends BotCommand {
    protected final Users users;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     * @param users             users storage
     */
    public UserInteractiveBotCommand(String commandIdentifier, String description, Users users) {
        super(commandIdentifier, description);
        this.users = users;
    }
}