package ru.daniil4jk.randomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.NoSuchElementException;

@Slf4j
public class Reports {
    static public void reportUnconnectedWriting(AbsSender absSender, long chatID) {
        log.trace("Пользователь " + getUserName(chatID) + " пытается отправить сообщение в пустоту");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Вы еще ни к кому не подключились, используйте команду \"/random\" чтобы подключиться к кому-либо")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportBusyUser(AbsSender absSender, long chatID, String findingNickname) {
        log.trace("Пользователь " + findingNickname + " которого ищет " + getUserName(chatID) + " сейчас общается с кем-то другим");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Пользователь с таким именем уже общается с кем-то другим, вы можете подождать пока он завершит диалог")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportIncorrectNickname(AbsSender absSender, long chatID, String findingNickname) {
        log.trace(getUserName(chatID) + " ввел некорректный никнейм " + findingNickname);
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Вы ввели некорректный никнейм, попробуйте снова")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportUnregisteredNickname(AbsSender absSender, long chatID, String findingNickname) {
        log.trace("Пользователь " + findingNickname + " которого ищет " + getUserName(chatID) + " еще не зарегистрирован");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Пользователь с таким именем еще не зарегистрирован у нас, но вы можете пригласить его")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportTimeOut(AbsSender absSender, long chatID) {
        log.trace("Свободные люди для" + getUserName(chatID) + "не найдены");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("К сожалению по вашим параметрам свободных людей нет, подождите и попробуйте еще раз, мы верим: вы сможете найти собеседника по душе")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportNeedRegistration(AbsSender absSender, Long chatid) {
        log.trace("Польтзователь без регистрации пытается написать");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatid)
                    .text("Для использования бота необходима регистрация(\nСделать это можно командой \"/start\"")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportNotNumber(AbsSender absSender, long chatID) {
        log.trace("Польтзователь пытается ввести не число, в поле, в котором необходимо число");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("То, что вы ввели, не является числом, попробуйте снова")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportEmptyAge(AbsSender absSender, long chatID) {
        log.trace("Пользователь пытается ввести не число, в поле, в котором необходимо число");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Вы не ввели возраст, попробуйте снова(")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportWaiting(AbsSender absSender, long chatID) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("Вы уже находитесь в поиске, подождите его окончания и попробуйте снова⏳")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportNeedPremium(AbsSender absSender, long chatID, String serviceName) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatID)
                    .text("У вас нет Premium подписки, которая необходима чтобы получить доступ к " + serviceName + " \uD83D\uDE14")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportGroupWriting(AbsSender absSender, long chatId) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("Наш бот гарантирует, что с каждым пользователем одновременно на линии будет только 1 пользователь, поэтому прейдите, пожалуйста, в личный чат, буду ждать вас там (если очень хочется пообщаться вдвоем с кем-то, то можете зайти с одного аккаунта, но если что - мы осуждаем такое\uD83D\uDE09)")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static UserService getUsers() {
        return ApplicationContextProvider.getApplicationContext().getBean(UserService.class);
    }

    private static String getUserName(long UID) {
        try {
            return getUsers().getProperties(UID).getUserName();
        } catch (NoSuchElementException e) {
            return "Незарегистрированный пользователь";
        }
    }
}