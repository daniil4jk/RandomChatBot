package ru.daniil4jk.randomChatBot.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "friends")
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long DBid;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private RandomChatBotUser randomChatBotUser;
    private String name;
    private long telegramId;

    public Friend(String name, long telegramId, RandomChatBotUser randomChatBotUser) {
        this.name = name;
        this.telegramId = telegramId;
        this.randomChatBotUser = randomChatBotUser;
    }

    @Override
    public String toString() {
        return "DBid in BD: " + DBid + "; name: " + name + "; DBid in telegram: " + telegramId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Friend incoming = (Friend) obj;
        return DBid == incoming.getDBid();
    }
}
