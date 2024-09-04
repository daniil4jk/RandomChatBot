package ru.daniil4jk.randomChatBot.constants;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.daniil4jk.randomChatBot.bots.AbstractBot;
import ru.daniil4jk.randomChatBot.constants.Emoji;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.NoSuchElementException;

@Slf4j
@Component
public class Reports {
    public static final String unconnectedWriting = "Вы еще ни к кому не подключились, используйте команду \"/random\" чтобы подключиться к кому-либо";
    public static final String busyUser = "Пользователь с таким именем уже общается с кем-то другим, вы можете подождать пока он завершит диалог";
    public static final String incorrectNickname = "Вы ввели некорректный никнейм, попробуйте снова";
    public static final String unregisteredNickname = "Пользователь с таким именем еще не зарегистрирован у нас, но вы можете пригласить его";
    public static final String timeOut = "К сожалению по вашим параметрам свободных людей нет, подождите и попробуйте еще раз, мы верим: вы сможете найти собеседника по душе";
    public static final String needRegistration = "Для использования бота необходима регистрация(\nСделать это можно командой \"/start\"";
    public static final String notNumber = "То, что вы ввели, не является числом, попробуйте снова";
    public static final String emptyAge = "Вы не ввели возраст, попробуйте снова(";
    public static final String waiting = "Ты уже находишься в поиске, подожди его окончания и попробуй снова⏳";
    public static final String needPremium = "У вас нет Premium подписки, которая необходима чтобы получить доступ к данной функции \uD83D\uDE14";
    public static final String groupWriting = "Наш бот гарантирует, что с каждым пользователем одновременно на линии будет только 1 пользователь, поэтому прейдите, пожалуйста, в личный чат, буду ждать вас там (если очень хочется пообщаться вдвоем с кем-то, то можете зайти с одного аккаунта, но если что - мы осуждаем такое\uD83D\uDE09)";
    public static final String friendExist = "Этот человек уже у тебя в друзьях";
    public static String illegalAge(Gender gender) {
        return "Ты ввел" + (gender == Gender.Girl ? "а" : "") + " неверный возраст";
    }
}