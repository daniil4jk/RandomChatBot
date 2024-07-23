package ru.daniil4jk.randomChatBot.Commands;

import org.springframework.stereotype.Component;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

@Component
public class SetMaxFindingAgeCommand extends SetAgeCommand {
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

    protected String getMessageText() {
        return "Введите максимальный возраст для поиска" + Emoji.MAX_FINDING_AGE;
    }

    @Override
    protected RandomChatBotUser setAge(RandomChatBotUser properties, int age) {
        properties.setEndRequiredAge(age);
        return properties;
    }
}
