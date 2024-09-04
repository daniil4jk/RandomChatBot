package ru.daniil4jk.randomChatBot.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter
public class CommandService {
    @Autowired
    private IBotCommand[] commands;
    Map<Class<? extends IBotCommand>, IBotCommand> commandMap = new HashMap<>();

    @PostConstruct
    private void plottingMap() {
        for (IBotCommand cmd : commands) {
            commandMap.put(cmd.getClass(), cmd);
        }
    }

    public <T extends IBotCommand> T getCommand(Class<T> t) {
        return (T) commandMap.get(t);
    }
}
