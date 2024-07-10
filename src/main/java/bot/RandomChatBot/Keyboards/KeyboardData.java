package bot.RandomChatBot.Keyboards;

import bot.RandomChatBot.Constants.Emoji;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum KeyboardData {
    START("GoReg"),
    RANDOM("Искать собеседника \uD83D\uDD25"),
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
    PREMIUM("Премиум " + Emoji.PREMIUM);

    private static final Map<String, KeyboardData> LOOKUP_MAP = new HashMap<>();

    static {
        for (KeyboardData d : values()) {
            LOOKUP_MAP.put(d.getData(), d);
        }
    }

    private final String data;

    KeyboardData(String data) {
        this.data = data;
    }

    public static boolean contains(KeyboardData constant) {
        return LOOKUP_MAP.containsValue(constant);
    }

    public static boolean contains(String constantValue) {
        return LOOKUP_MAP.containsKey(constantValue);
    }

    public static KeyboardData getConst(String constantValue) {
        return LOOKUP_MAP.get(constantValue);
    }
}

