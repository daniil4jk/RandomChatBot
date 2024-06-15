package bot.RandomChatBot.service;


import bot.RandomChatBot.Gender;
import bot.RandomChatBot.models.UserProperties;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class UserService {
    public static final String OVERRIDE = "mne_pohyi";
    public final HashMap<User, List<Message>> messages = new HashMap<>();
    public final HashMap<User, UserProperties> properties = new HashMap<>();
    public final ConcurrentHashMap<User, Timer> finders = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<User, User> pairs = new ConcurrentHashMap<>();
    public final HashMap<User, List<User>> friends = new HashMap<>();
    public final HashMap<Long, User> chatIDs = new HashMap<>();
    public final HashMap<User, Consumer<String>> waitingMessageEvents = new HashMap<>();

    public void addUser(User user, Chat chat) {
        chatIDs.put(chat.getId(), user);
        messages.put(user, new ArrayList<>());
        properties.put(user, new UserProperties());
    }

    {
        try {
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
        } catch (NoSuchElementException ignored) {
        }
    }
}
