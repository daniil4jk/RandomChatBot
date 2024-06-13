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