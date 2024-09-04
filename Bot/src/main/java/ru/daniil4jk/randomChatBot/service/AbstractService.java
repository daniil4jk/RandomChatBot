package ru.daniil4jk.randomChatBot.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;
import ru.daniil4jk.randomChatBot.others.BackupMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractService<K, E> {
    private final JpaRepository<E, K> repository;
    protected final Map<K, E> objectMap = new ConcurrentHashMap<>();
    private final BackupMap<K, E> copyOfObjectMap = new BackupMap<>(objectMap) {
        @Override
        public E cloneOriginalObject(E originalObject) {
            return AbstractService.this.cloneOriginalObject(originalObject);
        }
    };
    public int synchronizeWithDBInterval;

    public AbstractService(JpaRepository<E, K> repository) {
        this.repository = repository;
        createUpdateRepositoryThread();
    }

    private void createUpdateRepositoryThread() {
        new Thread(() -> {
            while (true) {
                copyOfObjectMap.checkForEquals(v -> {
                    repository.save(v);
                    log.debug("Изменения сохранены");
                    copyOfObjectMap.refresh();
                });
                try {
                    Thread.sleep(synchronizeWithDBInterval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    protected abstract E cloneOriginalObject(E originalObject);
}
