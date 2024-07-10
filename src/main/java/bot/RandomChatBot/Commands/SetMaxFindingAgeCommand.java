package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Constants.Emoji;
import bot.RandomChatBot.models.UserProperties;

public class SetMaxFindingAgeCommand extends SetAgeCommand {
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
    protected UserProperties setAge(UserProperties properties, int age) {
        properties.setEndRequiredAge(age);
        return properties;
    }
}
