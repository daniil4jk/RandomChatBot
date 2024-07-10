package bot.RandomChatBot.service;


import bot.RandomChatBot.BackupMap;
import bot.RandomChatBot.models.UserProperties;
import bot.RandomChatBot.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
@Slf4j
public class UserService {
    public static final String OVERRIDE = "override";
    private final ConcurrentHashMap<Long, UserProperties> propertiesMap = new ConcurrentHashMap<>();
    private final BackupMap<Long, UserProperties>
            copyOfPropertiesMap = new BackupUserPropertiesMap(propertiesMap);
    private final UserRepository userRepository;
    public final ConcurrentHashMap<Long, Timer> finders = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Long, Long> pairs = new ConcurrentHashMap<>();
    public final HashMap<Long, Consumer<Message>> waitingMessageEvents = new HashMap<>();
    public final HashMap<Long, User> UIDs = new HashMap<>();

    @PostConstruct
    private void createUpdateRepositoryThread() {
        new Thread(() -> {
            while (true) {
                final boolean[] needRefresh = {false};
                copyOfPropertiesMap.checkForEquals(v -> {
                    userRepository.save(v);
                    needRefresh[0] = true;
                });
                if (needRefresh[0]) {
                    log.debug("Изменения сохранены");
                    copyOfPropertiesMap.refresh();
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(long chatID) {
        propertiesMap.put(chatID, new UserProperties(chatID));
    }

    public UserProperties getProperties(Long UID) {
        if (propertiesMap.containsKey(UID)) {
            return propertiesMap.get(UID);
        }
        var properties = userRepository.findById(UID).orElseThrow();
        propertiesMap.put(UID, properties);
        copyOfPropertiesMap.putCopy(UID, properties);
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

    public UserProperties getPropertiesByUserName(String userName) {
        return userRepository.findByUserName(userName).orElseThrow();
    }

    @PreDestroy
    public void saveAll() {
        System.out.println("DESTROY");
        for (UserProperties p : propertiesMap.values()) {
            userRepository.save(p);
            System.out.println("SAVING: " + p);
        }
    }
}

class BackupUserPropertiesMap extends BackupMap<Long, UserProperties> {
    public BackupUserPropertiesMap(Map<Long, UserProperties> originalMap) {
        super(originalMap);
    }

    @Override
    public UserProperties cloneOriginalObject(UserProperties originalObject) {
        return new UserProperties(originalObject.getID(), originalObject.isRegistred(),
                originalObject.getUserName(), originalObject.getEndPremium(),
                originalObject.isPremium(), originalObject.getGender(),
                originalObject.getFindingGender(), originalObject.getAge(),
                originalObject.getStartFindingAge(), originalObject.getEndRequiredAge(),
                originalObject.getMessages(), originalObject.getFriends());
    }
}
