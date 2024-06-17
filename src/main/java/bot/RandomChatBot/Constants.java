package bot.RandomChatBot;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

enum KeyboardData {
    REGISTER_THREAD("regThread"),
    RANDOM("Искать собеседника \uD83D\uDD25"),
    FORM("Анкета " + Emoji.FORM),
    STOP("Остановить " + Emoji.STOP),
    SETTINGS("Настроить поиск " + Emoji.SETTINGS),
    SET_MALE_GENDER("setMaleGender"),
    SET_FEMALE_GENDER("setFemaleGender"),
    SET_GENDER("setGender"),
    SET_AGE("setAge"),
    SET_FINDING_GENDER("setFindingGender"),
    SET_MALE_FINDING_GENDER("setMaleFindingGender"),
    SET_FEMALE_FINDING_GENDER("setFemaleFindingGender"),
    SET_MIN_FIND_AGE("setMinFindAge"),
    SET_MAX_FIND_AGE("setMaxFindAge"),
    PREMIUM("Премиум " + Emoji.PREMIUM);

    private static final Map<String, KeyboardData> LOOKUP_MAP = new HashMap<>();

    static {
        for (KeyboardData d : values()) {
            LOOKUP_MAP.put(d.getData(), d);
        }
    }

    @Getter
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

class Emoji {
    public static final String START = "\uD83D\uDCAB";
    public static final String FORM = "\uD83C\uDF04";
    public static final String SETTINGS = "⚙\uFE0F";
    public static final String AGE = "\uD83D\uDD25";
    public static final String GENDER = "\uD83D\uDE0B";
    public static final String FINDING_GENDER = "\uD83D\uDE0F";
    public static final String MIN_FINDING_AGE = "⏬"; //!!"⏫⏬"
    public static final String MAX_FINDING_AGE = "⏫"; //!!"⏫⏬"
    public static final String RANDOM = "\uD83C\uDFB2";
    public static final String STOP = "❌";
    public static final String HELP = "\uD83D\uDCC4";
    public static final String PREMIUM = "\uD83C\uDF15";
}

