package ru.daniil4jk.randomChatBot.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@ConfigurationProperties(prefix="bot")
@PropertySource("config.properties")
public class BotConfig {
    @Value("${bot.names}")
    @Setter
    private String[] names;
    @Value("${bot.tokens}")
    @Setter
    private String[] tokens;
    @Value("${bot.admin.nameForUsers}")
    private String adminNameForUsers;
    @Value("${bot.admin.UID}")
    private long adminUID;
    @Value("${bot.admin.chatForMediaID}")
    private long mediaGroupId;
    @Value("${bot.premiumSystem.active}")
    private boolean premiumSystemActive;
    @Value("${bot.premiumSystem.renewMillsInterval}")
    private int premiumRenewInterval;
    @Value("${bot.synchronizeWithDBMillsInterval}")
    private int synchronizeWithDBInterval;
}

