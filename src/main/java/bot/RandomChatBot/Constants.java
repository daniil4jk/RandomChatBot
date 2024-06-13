package bot.RandomChatBot;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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

    @Getter
    private final String data;

    private KeyboardData(String data) {
        this.data = data;
    }

    private static final Map<String, KeyboardData> LOOKUP_MAP = new HashMap<>();

    static {
        for (KeyboardData d : values()) {
            LOOKUP_MAP.put(d.getData(), d);
        }
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

@Configuration
class Users {
    public final HashMap<User, List<Message>> messages = new HashMap<>();
    public final HashMap<User, UserProperties> properties = new HashMap<>();
    public final ConcurrentHashMap<User, Timer> finders = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<User, User> pairs = new ConcurrentHashMap<>();
    public final HashMap<User, List<User>> friends = new HashMap<>();
    public final HashMap<Long, User> chatIDs = new HashMap<>();
    public final HashMap<User, Consumer<String>> waitingMessageEvents = new HashMap<>();
    public static final String OVERRIDE = "mne_pohyi";

    {
        new Thread(() -> {
            final Scanner sc = new Scanner(System.in);
            while (true) {
                String input = sc.nextLine();
                switch (input) {
                    case "/getUsers" -> {
                        System.out.println("Количество зарегистрированных пользователей: " + finders.size());
                        for (User u : messages.keySet()) {
                            System.out.println(u);
                        }
                    }
                    case "/getFinders" -> {
                        System.out.println("Количество ищущих: " + finders.size());
                        for (User u : finders.keySet()) {
                            System.out.println(u);
                        }
                    }
                    case "/getFindersCount" -> System.out.println("Количество ищущих: " + finders.size());
                    case "/getCommunicating" -> {
                        System.out.println("Количество общающихся: " + pairs.size());
                        for (User u : pairs.keySet()) {
                            System.out.println(u);
                        }
                    }
                    case "/getCommunicatingCount" -> System.out.println("Количество общающихся: " + pairs.size());
                    case "/getActiveUsers" -> {
                        System.out.println("Количество активных пользователей: " + (finders.size() + pairs.size()));
                        for (User u : finders.keySet()) {
                            System.out.println(u);
                        }
                        for (User u : pairs.keySet()) {
                            System.out.println(u);
                        }
                    }
                    case "/getProperties" -> {
                        System.out.println("Количество пользователей имеющих характеристики " + properties.size());
                        for (Map.Entry<User, UserProperties> entry : properties.entrySet()) {
                            System.out.println(entry.getKey());
                            UserProperties v = entry.getValue();
                            System.out.println("Лет: " + v.getAge() + " Пол: " + Gender.formatToRusString(v.getGender()) +
                                    " Ищет от " + v.getStartFindingAge() + " до " + v.getEndRequiredAge() + " лет Искомый пол:" +
                                    Gender.formatToRusString(v.getFindingGender()));
                        }
                    }
                }
            }
        }).start();
    }
}