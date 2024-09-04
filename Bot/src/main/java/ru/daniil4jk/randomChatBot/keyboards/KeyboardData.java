package ru.daniil4jk.randomChatBot.keyboards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.daniil4jk.randomChatBot.constants.Emoji;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum KeyboardData {
    START("GoReg"),
    RANDOM("Искать собеседника " + Emoji.FIRE),
    FORM("Анкета " + Emoji.FORM),
    STOP("Остановить " + Emoji.STOP),
    SETTINGS("Настроить поиск " + Emoji.SETTINGS),
    SET_BOY_GENDER("setBoyGender"),
    SET_GIRL_GENDER("setGirlGender"),
    SET_GENDER("setGender"),
    SET_AGE("setAge"),
    SET_FINDING_GENDER("setFindingGender"),
    SET_BOY_FINDING_GENDER("setBoyFindingGender"),
    SET_GIRL_FINDING_GENDER("setGirlFindingGender"),
    SET_MIN_FIND_AGE("setMinFindAge"),
    SET_MAX_FIND_AGE("setMaxFindAge"),
    FRIEND_ACCEPT("Принять"),
    FRIEND_DENY("Отказать"),
    ADD_FRIEND("Добавить друга " + Emoji.WINKING),
    FRIENDS("Друзья " + Emoji.FRIENDS),
    PREMIUM("Премиум " + Emoji.PREMIUM);

    private static final Map<String, KeyboardData> LOOKUP_MAP = new HashMap<>();

    static {
        for (KeyboardData d : values()) {
            LOOKUP_MAP.put(d.getStringValue(), d);
        }
    }

    private final String stringValue;

    public static boolean contains(String constantValue) {
        return LOOKUP_MAP.containsKey(constantValue);
    }

    public static KeyboardData getConst(String constantValue) {
        return LOOKUP_MAP.get(constantValue);
    }
}

