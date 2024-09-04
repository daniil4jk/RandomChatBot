package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.keyboards.DefaultKeyboard;

@Slf4j
@Component
public class HelpCommand extends SimpleBotCommand {
    @Autowired
    DefaultKeyboard defaultKeyboard;

    public HelpCommand() {
        super("help", "Список всех команд " + Emoji.HELP);
    }

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
    public void execute(AbsSender absSender, long chatId, String[] arguments) {
        SendMessage help = SendMessage.builder()
                .text("====== Список всех команд ======\n" +
                        "\n/start - Запустить бота" + Emoji.START +
                        "\n/form - Ваша анкета" + Emoji.FORM +
                        "\n/setage - Установить возраст" + Emoji.AGE +
                        "\n/setgender - Выбрать пол" + Emoji.GENDER +
                        "\n/setfindinggender - Указать желаемый пол" + Emoji.FINDING_GENDER +
                        "\n/setminfindingage - Указать мин. возраст поиска" + Emoji.MIN_FINDING_AGE +
                        "\n/setmaxfindingage - Указать макс. возраст поиска" + Emoji.MAX_FINDING_AGE +
                        "\n/random - Найти случайного собеседника" + Emoji.RANDOM +
                        "\n/friends - Показать список друзей" + Emoji.FRIENDS +
                        "\n/stop - Остановить чат" + Emoji.STOP +
                        "\n/premium - Вывести меню покупки премиум подписки" + Emoji.PREMIUM +
                        "\n/help - Список всех команд" + Emoji.HELP +
                        "\n\n================================")
                .chatId(chatId)
                .replyMarkup(defaultKeyboard)
                .build();
        try {
            absSender.execute(help);
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить сообщение", e);
        }
    }
}
