package ru.daniil4jk.randomChatBot.Commands;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.Reports;
import ru.daniil4jk.randomChatBot.keyboards.PremiumKeyboard;
import ru.daniil4jk.randomChatBot.service.UserService;

@Component
public class PremiumCommand extends UserServiceIntegratedBotCommand {
    @Autowired
    private PremiumKeyboard premiumKeyboard;

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
    public void execute(AbsSender absSender, User user, @NotNull Chat chat, String[] strings) {
        if (!(getUsers().exist(chat.getId()) || strings != null &&
                strings.length > 0 && UserService.OVERRIDE_USER_PASS.equals(strings[0]))) {
            Reports.reportNeedRegistration(absSender, chat.getId());
            return;
        }
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chat.getId())
                    .text("Купить премиум подписку\nПодписка подключается моментально после покупки")
                    .replyMarkup(premiumKeyboard)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
