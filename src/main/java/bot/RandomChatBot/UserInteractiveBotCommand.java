package bot.RandomChatBot;

import bot.RandomChatBot.service.UserService;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;

public abstract class UserInteractiveBotCommand extends BotCommand {
    protected final UserService users;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public UserInteractiveBotCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
        this.users = ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
    }
}

@Component
class ApplicationContextProvider implements ApplicationContextAware {
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }
}