package ru.daniil4jk.randomChatBot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class PremiumKeyboard extends InlineKeyboardMarkup {
    private final InlineKeyboardButton PAY_DAY_BUTTON = new InlineKeyboardButton("Купить на день");
    private final InlineKeyboardButton PAY_WEEK_BUTTON = new InlineKeyboardButton("Купить на неделю");
    private final InlineKeyboardButton PAY_MOUNTH_BUTTON = new InlineKeyboardButton("Купить на месяц");
    private final List<List<InlineKeyboardButton>> BUTTONS;

    {
        BUTTONS = List.of(
                List.of(PAY_DAY_BUTTON),
                List.of(PAY_WEEK_BUTTON),
                List.of(PAY_MOUNTH_BUTTON)
        );
    }

    public PremiumKeyboard() {
        this.setKeyboard(BUTTONS);
    }

    private void createInvoiceLinks(AbsSender absSender) {
        try {
            PAY_DAY_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                    .title("Премиум 1 день")
                    .description("Все возможности премиум подписки на 1 день")
                    .payload("1")
                    .providerToken("381764678:TEST:87212")
                    .currency("RUB")
                    .price(new LabeledPrice("Цена премиума на ДЕНЬ (мин. возможная цена в тг!)", 7000))
                    .build()));
            PAY_WEEK_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                    .title("Премиум 1 неделя")
                    .description("Все возможности премиум подписки на 1 неделю")
                    .payload("7")
                    .providerToken("381764678:TEST:87212")
                    .currency("RUB")
                    .price(new LabeledPrice("Цена премиума на 7 ДНЕЙ", 20000))
                    .build()));
            PAY_MOUNTH_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                    .title("Премиум 1 месяц")
                    .description("Все возможности премиум подписки на 1 месяц")
                    .payload("31")
                    .providerToken("381764678:TEST:87212")
                    .currency("RUB")
                    .price(new LabeledPrice("Цена премиума на 31 ДЕНЬ", 50000))
                    .build()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}