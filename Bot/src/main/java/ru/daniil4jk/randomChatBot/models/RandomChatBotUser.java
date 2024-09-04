package ru.daniil4jk.randomChatBot.models;

import jakarta.persistence.*;
import lombok.*;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.others.FriendList;

import java.util.*;

@Entity
@Data
@AllArgsConstructor
@Table(name = "users")
@EqualsAndHashCode
@ToString
public class RandomChatBotUser {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "randomChatBotUser")
    List<Friend> friends = new FriendList();
    @Id
    @Column(name = "id")
    private long ID;
    private boolean registred = false;
    private String userName;
    private Date endPremium;
    private boolean premium;
    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.NotStated;
    @Enumerated(EnumType.STRING)
    private Gender findingGender = Gender.NotStated;
    private int age = -1;
    private int minFindingAge = -128;
    private int maxFindingAge = 127;

    public RandomChatBotUser(long userID) {
        this.ID = userID;
    }

    public FriendList getFriends() {
        return (FriendList) friends;
    }

    public void addPremium(int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        endPremium = calendar.getTime();
        premium = true;
    }

    public boolean isPremiumActivatedAtLeastOnce() {
        return endPremium != null;
    }
}

