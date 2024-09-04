package ru.daniil4jk.randomChatBot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Reports;
import ru.daniil4jk.randomChatBot.keyboards.PremiumKeyboard;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PremiumCommand extends UserServiceIntegratedBotCommand {
    Map<AbsSender, PremiumKeyboard> premiumKeyboard = new HashMap<>();

    public PremiumCommand() {
        super("premium", "Платная подписка");
    }

    /**
     * Construct a command
     *
     * @param commandIdentifier the unique identifier of this command (e.g. the command string to
     *                          enter into chat)
     * @param description       the description of this command
     */
    public PremiumCommand(String commandIdentifier, String description) {
        super(commandIdentifier, description);
    }

    @Override
    public void execute(AbsSender absSender, long chatId, String[] strings) {
        log.info("Вызван премиум");
        try {
            ((AbstractBot) absSender).sendSimpleTextMessage("Вызван премиум", chatId);
            absSender.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("==== Купить премиум подписку ====\n" +
                            "\n  Подписка подключается моментально\n  после покупки" +
                            Emoji.WINKING +
                            "\n\n=================================")
                    .replyMarkup(premiumKeyboard.computeIfAbsent(absSender, PremiumKeyboard::new))
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не получилось отправить сообщение", e);
        }
        throw new RuntimeException();
    }
}
