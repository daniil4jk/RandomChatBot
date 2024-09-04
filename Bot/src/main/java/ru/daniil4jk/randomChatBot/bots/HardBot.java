package ru.daniil4jk.randomChatBot.bots;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;

//@Component
public class HardBot extends AbstractBot {
    public HardBot(BotConfig config) {
        super(config.getTokens()[1], config.getNames()[1]);
    }

    @Override
    public InputFile getHelloPhoto() {
        return new InputFile("AgACAgIAAxkBAAIJk2Zjvk_Cf-4uznPnyMYMxwMpUe70AAKN2jEb_xUgS13GCak1cFjyAQADAgADeQADNQQ");
    }
}
