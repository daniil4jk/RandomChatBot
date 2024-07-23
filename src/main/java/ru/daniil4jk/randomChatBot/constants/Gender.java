package ru.daniil4jk.randomChatBot.constants;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Gender {
    NotStated("Не указан"),
    Girl("Женский"),
    Boy("Мужской");

    private final String rusString;

    public String toRusString() {
        return rusString;
    }
}
