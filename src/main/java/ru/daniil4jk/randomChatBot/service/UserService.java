package ru.daniil4jk.randomChatBot.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
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
    public final Map<Long, Timer> finders = new ConcurrentHashMap<>();
    public final Map<Long, Long> pairs = new ConcurrentHashMap<>();
    public final Map<Long, Long> friendRequests = new HashMap<>();
    public final Map<Long, Long> friendConnectRequests = new HashMap<>();
    public final Map<Long, Consumer<Message>> messageEvents = new HashMap<>();
    public final Map<Long, User> UIDs = new HashMap<>();
    private final Map<Long, RandomChatBotUser> propertiesMap = new ConcurrentHashMap<>();
    private final BackupMap<Long, RandomChatBotUser>
            copyOfPropertiesMap = new BackupUserPropertiesMap(propertiesMap);
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    private void createUpdateRepositoryThread() {
        new Thread(() -> {
            while (true) {
                copyOfPropertiesMap.checkForEquals(v -> {
                    System.out.println(v.getFriends());
                    userRepository.save(v);
                    log.debug("Изменения сохранены");
                    copyOfPropertiesMap.refresh();
                });
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void addUser(long chatID) {
        propertiesMap.put(chatID, new RandomChatBotUser(chatID));
    }

    public RandomChatBotUser getProperties(Long UID) {
        if (propertiesMap.containsKey(UID)) {
            return propertiesMap.get(UID);
        }
        var properties = userRepository.findById(UID).orElseThrow();
        propertiesMap.put(UID, properties);
        copyOfPropertiesMap.putCopy(UID, properties);
        userRepository.flush();
        return properties;
    }

    public boolean exist(long UID) {
        try {
            getProperties(UID);
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
        System.out.println("DESTROY");
        for (RandomChatBotUser p : propertiesMap.values()) {
            userRepository.save(p);
            System.out.println("SAVING: " + p);
        }
    }
}

class BackupUserPropertiesMap extends BackupMap<Long, RandomChatBotUser> {
    public BackupUserPropertiesMap(Map<Long, RandomChatBotUser> originalMap) {
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
                originalObject.getStartFindingAge(), originalObject.getEndRequiredAge());
    }
}

abstract class BackupMap<K, V> extends HashMap<K, V> {
    private final Map<K, V> originalMap;

    public BackupMap(Map<K, V> originalMap) {
        this.originalMap = originalMap;
        fillMap(originalMap);
    }

    public void refresh() {
        refresh(originalMap);
    }

    public void refresh(Map<K, V> originalMap) {
        fillMap(originalMap);
    }

    private void fillMap(Map<K, V> mapToFill) {
        for (Entry<K, V> e : mapToFill.entrySet()) {
            if (!e.getValue().equals(this.get(e.getKey()))) {
                remove(e.getKey(), e.getValue());
                put(e.getKey(), cloneOriginalObject(e.getValue()));
            }
        }
    }

    public boolean checkForEquals() {
        return checkForEquals(null);
    }

    public boolean checkForEquals(Consumer<V> actionForDifference) {
        boolean equals = true;
        for (K key : originalMap.keySet()) {
            if (!ValueEquals(originalMap.get(key), get(key))) {
                equals = false;
                if (actionForDifference != null) actionForDifference.accept(originalMap.get(key));
            }
        }
        return equals;
    }

    public void putCopy(K key, V value) {
        put(key, cloneOriginalObject(value));
    }

    abstract public V cloneOriginalObject(V originalObject);

    public boolean ValueEquals(V incoming, V backuped) {
        return incoming.equals(backuped);
    }
}
