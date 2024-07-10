package bot.RandomChatBot.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("config.properties")
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;
    @Value("${bot.admin.UID}")
    long adminUID;
    @Value("${bot.admin.groupID}")
    long mediaGroupId;
    @Value("${bot.premiumSystem.active}")
    boolean premiumSystemActive;
}
