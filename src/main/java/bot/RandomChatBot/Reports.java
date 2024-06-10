package bot.RandomChatBot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
class Reports {
    static public void reportUnconnectedWriting(AbsSender absSender, User user) {
        log.trace("Пользователь " + user.getUserName() + " пытается отправить сообщение в пустоту");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Вы еще ни к кому не подключились, используйте команду \"/random\" чтобы подключиться к кому-либо")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportBusyUser(AbsSender absSender, User user, String findingNickname) {
        log.trace("Пользователь " + findingNickname + " которого ищет " + user.getUserName() + " сейчас общается с кем-то другим");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Пользователь с таким именем уже общается с кем-то другим, вы можете подождать пока он завершит диалог")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportIncorrectNickname(AbsSender absSender, User user, String findingNickname) {
        log.trace(user.getUserName() + " ввел некорректный никнейм " + findingNickname);
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Вы ввели некорректный никнейм, попробуйте снова")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportUnregisteredNickname(AbsSender absSender, User user, String findingNickname) {
        log.trace("Пользователь " + findingNickname + " которого ищет " + user.getUserName() + " еще не зарегистрирован");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Пользователь с таким именем еще не зарегистрирован у нас, но вы можете пригласить его")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    static public void reportTimeOut(AbsSender absSender, User user) {
        log.trace("Свободные люди для" + user.getUserName() + "не найдены");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
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

    public static void reportNotNumber(AbsSender absSender, User user) {
        log.trace("Польтзователь пытается ввести не число, в поле, в котором необходимо число");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("То, что вы ввели, не является числом, попробуйте снова")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportEmptyAge(AbsSender absSender, User user) {
        log.trace("Пользователь пытается ввести не число, в поле, в котором необходимо число");
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Вы не ввели возраст, попробуйте снова(")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportWaiting(AbsSender absSender, User user) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("Вы уже находитесь в поиске, подождите его окончания и попробуйте снова⏳")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }

    public static void reportNeedPremium(AbsSender absSender, User user, String serviceName) {
        try {
            absSender.execute(SendMessage.builder()
                    .chatId(user.getId())
                    .text("У вас нет Premium подписки, которая необходима чтобы получить доступ к " + serviceName + " \uD83D\uDE14")
                    .build());
        } catch (TelegramApiException e2) {
            log.warn("Не получилось отправить сообщение", e2);
        }
    }
}