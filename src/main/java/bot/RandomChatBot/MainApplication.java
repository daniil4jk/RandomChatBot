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
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
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

@Configuration
class Users {
	final HashMap<User, List<Message>> messages = new HashMap<>();
	final HashMap<User, UserProperties> properties = new HashMap<>();
	final ConcurrentHashMap<User, Timer> finders = new ConcurrentHashMap<>();
	final ConcurrentHashMap<User, User> pairs = new ConcurrentHashMap<>();
	final HashMap<User, List<User>> friends = new HashMap<>();
	final HashMap<Long, User> chatIDs = new HashMap<>();
	final HashMap<User, Consumer<String>> waitingMessageEvents = new HashMap<>();
	final String OVERRIDE = "mne_pohyi";

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

abstract class UserInteractiveBotCommand extends BotCommand {
	protected final Users users;
	/**
	 * Construct a command
	 *
	 * @param commandIdentifier the unique identifier of this command (e.g. the command string to
	 *                          enter into chat)
	 * @param description       the description of this command
	 * @param users 			users storage
	 */
	public UserInteractiveBotCommand(String commandIdentifier, String description, Users users) {
		super(commandIdentifier, description);
		this.users = users;
	}
}