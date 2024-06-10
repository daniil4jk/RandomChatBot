package bot.RandomChatBot;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@SpringBootApplication
public class MainApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
}

@Getter
@Configuration
@PropertySource("config.properties")
class BotConfig {
	@Value("${bot.name}") String botName;
	@Value("${bot.token}") String token;
	@Value("${bot.admin.UID}") long adminUID;
	@Value("${bot.admin.groupID}") long mediaGroupId;
	@Value("${bot.premiumSystem.active}") boolean premiumSystemActive;
}

@Component
class Initializer {
	@Autowired
	Bot bot;

	@EventListener({ContextRefreshedEvent.class})
	public void init() throws TelegramApiException {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		telegramBotsApi.registerBot(bot);
    }
}

class Users {
	static final HashMap<User, List<Message>> messages = new HashMap<>();
	static final HashMap<User, UserProperties> properties = new HashMap<>();
	static final ConcurrentHashMap<User, Timer> finders = new ConcurrentHashMap<>();
	static final ConcurrentHashMap<User, User> pairs = new ConcurrentHashMap<>();
	static final HashMap<Long, User> chatIDs = new HashMap<>();
	static final HashMap<User, Consumer<String>> waitingMessageEvents = new HashMap<>();
	public static final String OVERRIDE = "mne_pohyi";

	static {
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

class KeyboardConstants {
	public static final String REGISTER_THREAD = "regThread";
	public static final String RANDOM = "Искать собеседника \uD83D\uDD25";
	public static final String FORM = "Анкета " + EmojiConstants.FORM;
	public static final String STOP = "Остановить " + EmojiConstants.STOP;
	public static final String SETTINGS = "Настроить поиск " + EmojiConstants.SETTINGS;
	public static final String SET_MALE_GENDER = "setMaleGender";
	public static final String SET_FEMALE_GENDER = "setFemaleGender";
	public static final String SET_GENDER = "setGender";
	public static final String SET_AGE = "setAge";
	public static final String SET_FINDING_GENDER = "setFindingGender";
	public static final String SET_MALE_FINDING_GENDER = "setMaleFindingGender";
	public static final String SET_FEMALE_FINDING_GENDER = "setFemaleFindingGender";
	public static final String SET_MIN_FIND_AGE = "setMinFindAge";
	public static final String SET_MAX_FIND_AGE = "setMaxFindAge";
	public static final String PREMIUM = "Премиум " + EmojiConstants.PREMIUM;
}

class EmojiConstants {
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