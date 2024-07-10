package bot.RandomChatBot;

import bot.RandomChatBot.models.UserProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BackupMap<K, V> extends HashMap<K, V> {
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
        for (Map.Entry<K, V> e : mapToFill.entrySet()) {
            if (!e.getValue().equals(this.get(e.getKey()))) {
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

    public V putCopy(K key, V value) {
        return put(key, cloneOriginalObject(value));
    }

    abstract public V cloneOriginalObject(V originalObject);
    public boolean ValueEquals(V incoming, V backuped) {
        return incoming.equals(backuped);
    }
}