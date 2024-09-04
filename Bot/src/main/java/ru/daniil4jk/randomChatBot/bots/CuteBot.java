package ru.daniil4jk.randomChatBot.bots;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;

@Component
public class CuteBot extends AbstractBot {
    public CuteBot(BotConfig config) {
        super(config.getTokens()[0], config.getNames()[0]);
    }

    @Override
    public InputFile getHelloPhoto() {
        return new InputFile("AgACAgIAAxkBAAIJk2Zjvk_Cf-4uznPnyMYMxwMpUe70AAKN2jEb_xUgS13GCak1cFjyAQADAgADeQADNQQ");
    }
}
