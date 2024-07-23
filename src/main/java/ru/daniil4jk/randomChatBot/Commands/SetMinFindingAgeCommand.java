package ru.daniil4jk.randomChatBot.Commands;

import org.springframework.stereotype.Component;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

@Component
public class SetMinFindingAgeCommand extends SetAgeCommand {
    public SetMinFindingAgeCommand() {
        super("setminfindingage", "Указать мин. возраст поиска " + Emoji.MIN_FINDING_AGE);
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public SetMinFindingAgeCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    protected String getMessageText() {
        return "Введите минимальный возраст для поиска" + Emoji.MIN_FINDING_AGE;
    }

    @Override
    protected RandomChatBotUser setAge(RandomChatBotUser properties, int age) {
        properties.setStartFindingAge(age);
        return properties;
    }
}
