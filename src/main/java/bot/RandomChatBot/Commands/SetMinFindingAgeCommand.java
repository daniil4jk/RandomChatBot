package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Constants.Emoji;
import bot.RandomChatBot.models.UserProperties;

public class SetMinFindingAgeCommand extends SetAgeCommand {
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
    protected UserProperties setAge(UserProperties properties, int age) {
        properties.setStartFindingAge(age);
        return properties;
    }
}
