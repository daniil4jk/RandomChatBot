package ru.daniil4jk.randomChatBot.controllers;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.Date;
import java.util.Map;

@Slf4j
@Getter
@Controller
public class PremiumController {
    public final int renewPremiumInterval;
    public final boolean premiumSystemActive;
    private final Map<Long, RandomChatBotUser> userMap;
    private Date currentDate = new Date();

    public PremiumController(BotConfig config, UserService userService) {
        premiumSystemActive = config.isPremiumSystemActive();
        userMap = userService.getUserMap();
        renewPremiumInterval = config.getPremiumRenewInterval();
    }

    @PostConstruct
    private void createPremiumRenewThread() {
        if (premiumSystemActive) {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(renewPremiumInterval);
                    } catch (InterruptedException e) {
                        log.error("Поток обновления премиума был прерван!", e);
                    }
                    currentDate = new Date();
                    for (RandomChatBotUser user : userMap.values()) {
                        if (user.isPremium() && (user.getEndPremium() == null || currentDate.after(user.getEndPremium()))) {
                            user.setPremium(false);
                            log.trace("Пользователь " + user.getUserName() + " лишен премиума");
                        }
                    }
                }
            }).start();
        }
    }
}
