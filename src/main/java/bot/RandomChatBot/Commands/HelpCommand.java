package bot.RandomChatBot.Commands;

import bot.RandomChatBot.Constants.Emoji;
import bot.RandomChatBot.Keyboards.DefaultKeyboard;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class HelpCommand extends BotCommand {
    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public HelpCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, org.telegram.telegrambots.meta.api.objects.User user, Chat chat, String[] arguments) {
        SendMessage help = SendMessage.builder()
                .text("====== Список всех команд ======" +
                        "\n\n/start - Запустить бота" + Emoji.START +
                        "\n/form - Ваша анкета" + Emoji.FORM +
                        "\n/setage - Установить возраст" + Emoji.AGE +
                        "\n/setgender - Выбрать пол" + Emoji.GENDER +
                        "\n/setfindinggender - Указать желаемый пол" + Emoji.FINDING_GENDER +
                        "\n/setminfindingage - Указать мин. возраст поиска" + Emoji.MIN_FINDING_AGE +
                        "\n/setmaxfindingage - Указать макс. возраст поиска" + Emoji.MAX_FINDING_AGE +
                        "\n/random - Найти случайного собеседника" + Emoji.RANDOM +
                        "\n/stop - Остановить чат" + Emoji.STOP +
                        "\n/help - Список всех команд" + Emoji.HELP +
                        "\n\n================================")
                .chatId(chat.getId())
                .replyMarkup(new DefaultKeyboard())
                .build();
        try {
            absSender.execute(help);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
