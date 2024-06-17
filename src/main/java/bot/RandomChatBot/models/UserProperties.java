package bot.RandomChatBot.models;

import bot.RandomChatBot.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@Table(name = "properties")
public class UserProperties {
    private static final ArrayList<UserProperties> allProperties = new ArrayList<>();
    @Getter
    public static final boolean premiumSystemActive = true;
    private static Date currentDate = new Date();

    static {
        if (premiumSystemActive) {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        log.error("Поток обновления даты и времени был прерван!", e);
                    }
                    currentDate = new Date();
                    for (UserProperties p : allProperties) {
                        if (p.premium && currentDate.after(p.endPremium)) {
                            p.premium = false;
                        }
                    }
                }
            }).start();
        }
    }

    @Id
    private Long id;

    private Date endPremium;
    private boolean premium = !premiumSystemActive;
    @Enumerated(EnumType.STRING)
    private Gender gender = Gender.NotStated;
    @Enumerated(EnumType.STRING)
    private Gender findingGender = Gender.NotStated;
    private byte age = 0;
    private byte startFindingAge = -128;
    private byte endRequiredAge = 127;

    public UserProperties(Long userID) {
        this.id = userID;
    }

    public void addPremium(int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(field, amount);
        endPremium = calendar.getTime();
        calendar.setTime(currentDate);
        premium = true;
    }

    public boolean isPremiumActivatedAtLeastOnce() {
        return endPremium != null;
    }

    public boolean isGenderNotStated() {
        return gender == Gender.NotStated;
    }

    public void setGender(Gender gender) {
        if (gender.equals(Gender.NotStated)) {
            throw new IllegalArgumentException("Пол не может быть пуст");
        }
        this.gender = gender;
    }

    public int getAge() {
        int intAge = age;
        return intAge + 128;
    }

    public void setAge(int age) {
        age -= 128;
        if (age <= 127 && age >= -128) {
            this.age = (byte) age;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getStartFindingAge() {
        int intStartRequiredAge = startFindingAge;
        return intStartRequiredAge + 128;
    }

    public void setStartFindingAge(int startFindingAge) {
        startFindingAge -= 128;
        if (startFindingAge <= 127 && startFindingAge >= -128) {
            this.startFindingAge = (byte) startFindingAge;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getEndRequiredAge() {
        int intEndRequiredAge = endRequiredAge;
        return intEndRequiredAge + 128;
    }

    public void setEndRequiredAge(int endRequiredAge) {
        endRequiredAge -= 128;
        if (endRequiredAge <= 127 && endRequiredAge >= -128) {
            this.endRequiredAge = (byte) endRequiredAge;
        } else {
            throw new IllegalArgumentException();
        }
    }
}