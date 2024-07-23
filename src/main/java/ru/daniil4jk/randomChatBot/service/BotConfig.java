package ru.daniil4jk.randomChatBot.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("config.properties")
public class BotConfig {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String token;
    @Value("${bot.admin.nameForUsers}")
    private String adminNameForUsers;
    @Value("${bot.admin.UID}")
    private long adminUID;
    @Value("${bot.admin.chatForMediaID}")
    private long mediaGroupId;
    @Value("${bot.premiumSystem.active}")
    private boolean premiumSystemActive;
    @Value("${bot.premiumSystem.renewInterval}")
    private int premiumRenewInterval;
    @Value("${bot.synchronizeWithDBInterval}")
    private int synchronizeWithDBInterval;
}
