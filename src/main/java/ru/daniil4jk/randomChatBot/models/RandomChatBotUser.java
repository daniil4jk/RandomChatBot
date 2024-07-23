package ru.daniil4jk.randomChatBot.models;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.daniil4jk.randomChatBot.constants.Gender;
import ru.daniil4jk.randomChatBot.service.BotConfig;
import ru.daniil4jk.randomChatBot.service.UserService;

import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class RandomChatBotUser {
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "randomChatBotUser")
    List<Friend> friends = new ArrayList<>();
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
    private int startFindingAge = -128;
    private int endRequiredAge = 127;
    @Transactional
    public List<Friend> getFriends() {
        return friends;
    }

    public RandomChatBotUser(long userID) {
        this.ID = userID;
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

    @Override
    public String toString() {
        return "id: " + ID + "; userName: " + userName + "; endPremium: " + endPremium + "; isPremium: " + premium +
                "; gender: " + gender + "; findingGender: " + findingGender + "; age: " + age + "; startFindingAge: " +
                startFindingAge + "; endFindingAge: " + endRequiredAge + "; friends: " + friends;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        RandomChatBotUser incoming = (RandomChatBotUser) obj;
        return  ID == ID
                &&
                (userName == null & incoming.getUserName() == null ||
                        (userName != null && userName.equals(incoming.getUserName())))
                &&
                (endPremium == null & incoming.getEndPremium() == null ||
                        (endPremium != null && endPremium.equals(incoming.getEndPremium())))
                &&
                registred == incoming.isRegistred()
                &&
                premium == incoming.isPremium()
                &&
                (gender == null & incoming.getGender() == null ||
                        (gender != null && gender.equals(incoming.getGender())))
                &&
                (findingGender == null & incoming.getFindingGender() == null ||
                        (findingGender != null && findingGender.equals(incoming.getFindingGender())))
                &&
                age == incoming.getAge()
                &&
                startFindingAge == incoming.getStartFindingAge()
                &&
                endRequiredAge == incoming.getEndRequiredAge()
                &&
                equalsFriends(incoming.getFriends());
    }

    private boolean equalsFriends(List<Friend> friends) {
        if (this.friends.size() != friends.size()) {
            return false;
        }
        Iterator<Friend> thisIterator = this.friends.iterator();
        Iterator<Friend> incomingIterator = friends.iterator();
        try {
            while (thisIterator.hasNext() || incomingIterator.hasNext()) {
                if (thisIterator.next() != incomingIterator.next()) {
                    return false;
                }
            }
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }
}

