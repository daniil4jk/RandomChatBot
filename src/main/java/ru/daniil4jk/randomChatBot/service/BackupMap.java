package ru.daniil4jk.randomChatBot.service;

import org.hibernate.Hibernate;
import ru.daniil4jk.randomChatBot.models.RandomChatBotUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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

    static class BackupUserPropertiesMap extends BackupMap<Long, RandomChatBotUser> {
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
}
