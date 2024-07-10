package bot.RandomChatBot.Commands;

import bot.RandomChatBot.ApplicationContextProvider;
import bot.RandomChatBot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;

@Slf4j
public abstract class UserServiceIntegratedBotCommand extends BotCommand {
    protected final UserService users;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public UserServiceIntegratedBotCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
        if (ApplicationContextProvider.getApplicationContext() != null) {
            this.users = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
        } else {
            users = null;
            log.error("Невозможно создать ссылку на Bean UserService в UserServiceIntegratedBotCommand, т.к. ApplicationContext в ApplicationContextProvider = null", new IllegalArgumentException("ApplicationContext is null!"));
            System.exit(-1);
        }
    }
}

