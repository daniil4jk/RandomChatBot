package bot.RandomChatBot.models;

import bot.RandomChatBot.Constants.Gender;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
@Entity
@AllArgsConstructor
@Table(name = "users")
@Getter
@Setter
public class UserProperties {
    public final static boolean premiumSystemActive = true;

    @Id
    @Column(name = "id")
    private Long ID;
    private boolean registred = false;

    private String userName;

    private Date endPremium;
    private boolean premium = !premiumSystemActive;

    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.NotStated;
    @Enumerated(EnumType.STRING)
    private Gender findingGender = Gender.NotStated;

    private int age = -1;
    private int startFindingAge = -128;
    private int endRequiredAge = 127;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(joinColumns = @JoinColumn(name = "users_id"))
    List<String> messages = new ArrayList<>();

    @ElementCollection(targetClass = Long.class, fetch = FetchType.EAGER)
    @CollectionTable(joinColumns = @JoinColumn(name = "users_id"))
    List<Long> friends = new ArrayList<>();
    public UserProperties(Long userID) {
        this.ID = userID;
        PremiumController.addUser(this);
    }

    public UserProperties() {
        PremiumController.addUser(this);
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
    public boolean isGenderNotStated() {
        return gender.equals(Gender.NotStated);
    }
    @Override
    public String toString() {
        return "id: " + ID + "; userName: " + userName + "; endPremium: " + endPremium + "; isPremium: " + premium +
                "; gender: " + gender + "; findingGender: " + findingGender + "; age: " + age + "; startFindingAge: " +
            startFindingAge + "; endFindingAge: " + endRequiredAge;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        UserProperties incoming = (UserProperties) obj;
        return (ID == null & incoming.getID() == null ||
                (ID != null && ID.equals(incoming.getID())))
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
                (friends == null & incoming.getFriends() == null ||
                (friends != null && friends.equals(incoming.getFriends())));
    }
}