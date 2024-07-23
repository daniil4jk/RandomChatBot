package ru.daniil4jk.randomChatBot.Commands;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import ru.daniil4jk.randomChatBot.service.UserService;

@Getter
@Slf4j
public abstract class UserServiceIntegratedBotCommand extends BotCommand {
    @Autowired
    private UserService users;

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public UserServiceIntegratedBotCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }
}

