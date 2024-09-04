package ru.daniil4jk.randomChatBot.keyboards;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
public class PremiumKeyboard extends InlineKeyboardMarkup {
    private final InlineKeyboardButton PAY_DAY_BUTTON = new InlineKeyboardButton("Премиум на день - 70 РУБ");
    private final InlineKeyboardButton PAY_WEEK_BUTTON = new InlineKeyboardButton("Премиум на неделю - 200 РУБ");
    private final InlineKeyboardButton PAY_MOUNTH_BUTTON = new InlineKeyboardButton("Премиум на месяц - 500 РУБ");
    private final List<List<InlineKeyboardButton>> BUTTONS;
    @Getter
    private boolean invoiceLinksCreated = false;

    {
        BUTTONS = List.of(
                List.of(PAY_DAY_BUTTON),
                List.of(PAY_WEEK_BUTTON),
                List.of(PAY_MOUNTH_BUTTON)
        );
    }

    public PremiumKeyboard(AbsSender absSender) {
        createInvoiceLinks(absSender);
        this.setKeyboard(BUTTONS);
    }

    public void createInvoiceLinks(AbsSender absSender) {
        try {
            PAY_DAY_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                    .title("Премиум 1 день")
                    .description("Возможность видеть данные собеседника и устанавливать фильтры поиска на 1 день")
                    .payload("1")
                    .providerToken("381764678:TEST:87212")
                    .currency("RUB")
                    .price(new LabeledPrice("Премиум на 1 ДЕНЬ", 7000))
                    .build()));
            PAY_WEEK_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                    .title("Премиум 1 неделя")
                    .description("Возможность видеть данные собеседника и устанавливать фильтры поиска на 1 неделю")
                    .payload("7")
                    .providerToken("381764678:TEST:87212")
                    .currency("RUB")
                    .price(new LabeledPrice("Премиум на 7 ДНЕЙ", 20000))
                    .build()));
            PAY_MOUNTH_BUTTON.setUrl(absSender.execute(CreateInvoiceLink.builder()
                    .title("Премиум 1 месяц")
                    .description("Возможность видеть данные собеседника и устанавливать фильтры поиска на 1 месяц")
                    .payload("31")
                    .providerToken("381764678:TEST:87212")
                    .currency("RUB")
                    .price(new LabeledPrice("Премиум на 31 ДЕНЬ", 50000))
                    .build()));
            invoiceLinksCreated = true;
        } catch (TelegramApiException e) {
            log.warn("Не получилось отправить invoice links", e);
        }
    }
}