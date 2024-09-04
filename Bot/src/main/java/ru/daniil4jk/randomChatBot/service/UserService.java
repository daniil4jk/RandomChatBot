package ru.daniil4jk.randomChatBot.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.daniil4jk.randomChatBot.configuration.BotConfig;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.others.BackupMap;
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
    public final Map<Long, Long> friendRequests = new ConcurrentHashMap<>();
    public final Map<Long, Long> friendConnectRequests = new ConcurrentHashMap<>();
    public final Map<Long, Queue<Consumer<Message>>> messageEvents = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    @Getter
    private final Map<Long, RandomChatBotUser> userMap = new ConcurrentHashMap<>();
    private final BackupMap<Long, RandomChatBotUser> copyOfUserMap = new BackupUserMap(userMap);

    public UserService(UserRepository userRepository, @NotNull BotConfig config) {
        this.userRepository = userRepository;
        synchronizeWithDBInterval = config.getSynchronizeWithDBInterval();
    }

    @PostConstruct
    private void createUpdateRepositoryThread() {
        new Thread(() -> {
            while (true) {
                copyOfUserMap.checkForEquals(v -> {
                    userRepository.save(v);
                    log.debug("Изменения сохранены");
                    copyOfUserMap.refresh();
                });
                try {
                    Thread.sleep(synchronizeWithDBInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    //@PostConstruct
    private void clearDB() {
        userRepository.deleteAll();
    }

    public void addUser(long chatID) {
        userMap.put(chatID, new RandomChatBotUser(chatID));
    }

    public RandomChatBotUser getUser(Long UID) {
        if (userMap.containsKey(UID)) {
            return userMap.get(UID);
        }
        var user = userRepository.findById(UID).orElseThrow();
        userMap.put(UID, user);
        copyOfUserMap.putCopy(UID, user);
        return user;
    }

    public boolean contains(long UID) {
        try {
            getUser(UID);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public RandomChatBotUser getUserByUsername(String userName) {
        for (RandomChatBotUser user : userMap.values()) {
            if (user.getUserName().equals(userName)) {
                return user;
            }
        }
        var user = userRepository.findByUserName(userName).orElseThrow();
        userMap.put(user.getID(), user);
        copyOfUserMap.putCopy(user.getID(), user);
        return user;
    }

    @PreDestroy
    public void saveAll() {
        int i = 0;
        for (RandomChatBotUser p : userMap.values()) {
            userRepository.save(p);
            log.trace("saving " + i + " user");
            i++;
        }
    }
}

class BackupUserMap extends BackupMap<Long, RandomChatBotUser> {
    public BackupUserMap(Map<Long, RandomChatBotUser> originalMap) {
        super(originalMap);
    }

    @Override
    public RandomChatBotUser cloneOriginalObject(RandomChatBotUser originalObject) {
        return new RandomChatBotUser((Hibernate.isInitialized(originalObject.getFriends()) ?
                new ArrayList<>(originalObject.getFriends()) : null),
                originalObject.getID(), originalObject.isRegistred(),
                originalObject.getUserName(), originalObject.getEndPremium(),
                originalObject.isPremium(), originalObject.getGender(),
                originalObject.getFindingGender(), originalObject.getAge(),
                originalObject.getMinFindingAge(), originalObject.getMaxFindingAge());
    }
}

