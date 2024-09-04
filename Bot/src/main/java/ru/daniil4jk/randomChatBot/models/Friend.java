package ru.daniil4jk.randomChatBot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Data
@Table(name = "friends")
@EqualsAndHashCode
@ToString
@IdClass(FriendId.class)
public class Friend {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", nullable = false)
    private RandomChatBotUser randomChatBotUser;
    private String name;
    @Id
    private long id;

    public Friend(String name, long id, RandomChatBotUser randomChatBotUser) {
        this.name = name;
        this.id = id;
        this.randomChatBotUser = randomChatBotUser;
    }
}

@Data
class FriendId {
    private RandomChatBotUser randomChatBotUser;
    private long id;
}
