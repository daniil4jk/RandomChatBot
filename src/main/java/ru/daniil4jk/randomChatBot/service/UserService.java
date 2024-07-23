package ru.daniil4jk.randomChatBot.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@Slf4j
public class UserService {
    public static final String OVERRIDE_USER_PASS = "override";
    public static final String OVERRIDE_ADMIN_PASS = "admin";
    public final int synchronizeWithDBInterval;
    public final Map<Long, Timer> finders = new ConcurrentHashMap<>();
    public final Map<Long, Long> pairs = new ConcurrentHashMap<>();
    public final Map<Long, Long> friendRequests = new HashMap<>();
    public final Map<Long, Long> friendConnectRequests = new HashMap<>();
    public final Map<Long, Consumer<Message>> messageEvents = new HashMap<>();
    public final Map<Long, User> UIDs = new HashMap<>();
    @Getter
    private final Map<Long, RandomChatBotUser> RCBUsersMap = new ConcurrentHashMap<>();
    private final BackupMap<Long, RandomChatBotUser>
            copyOfPropertiesMap = new BackupMap.BackupUserPropertiesMap(RCBUsersMap);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, BotConfig config) {
        this.userRepository = userRepository;
        synchronizeWithDBInterval = config.getSynchronizeWithDBInterval();
    }

    @PostConstruct
    private void createUpdateRepositoryThread() {
        new Thread(() -> {
            while (true) {
                copyOfPropertiesMap.checkForEquals(v -> {
                    userRepository.save(v);
                    log.debug("Изменения сохранены");
                    copyOfPropertiesMap.refresh();
                });
                try {
                    Thread.sleep(synchronizeWithDBInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void addUser(long chatID) {
        RCBUsersMap.put(chatID, new RandomChatBotUser(chatID));
    }

    public RandomChatBotUser getRCBUser(Long UID) {
        if (RCBUsersMap.containsKey(UID)) {
            return RCBUsersMap.get(UID);
        }
        var properties = userRepository.findById(UID).orElseThrow();
        RCBUsersMap.put(UID, properties);
        copyOfPropertiesMap.putCopy(UID, properties);
        userRepository.flush();
        return properties;
    }

    public boolean exist(long UID) {
        try {
            getRCBUser(UID);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public RandomChatBotUser getPropertiesByUserName(String userName) {
        return userRepository.findByUserName(userName).orElseThrow();
    }

    @PreDestroy
    public void saveAll() {
        int i = 0;
        for (RandomChatBotUser p : RCBUsersMap.values()) {
            userRepository.save(p);
            log.trace("saving " + i + " user");
            i++;
        }
    }
}

