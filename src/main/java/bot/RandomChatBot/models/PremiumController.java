package bot.RandomChatBot.models;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

@Slf4j
@Component
class PremiumController {
    private static final ArrayList<UserProperties> propsList = new ArrayList<>();
    private static Date currentDate = new Date();

    static {
        if (UserProperties.premiumSystemActive) {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        log.error("Поток обновления даты и времени был прерван!", e);
                    }
                    currentDate = new Date();
                    for (UserProperties u : propsList) {
                        if (u.isPremium() && currentDate.after(u.getEndPremium())) {
                            u.setPremium(false);
                            log.trace("У пользователя " + u + " закончился premium");
                        }
                    }
                }
            }).start();
        }
    }

    public static void addUser(UserProperties userProperties) {
        propsList.add(userProperties);
    }
}
