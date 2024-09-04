package ru.daniil4jk.randomChatBot.commands;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import ru.daniil4jk.randomChatBot.service.UserService;

@Getter
@Slf4j
public abstract class UserServiceIntegratedBotCommand extends SimpleBotCommand {
    @Autowired
    private UserService userService;

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

