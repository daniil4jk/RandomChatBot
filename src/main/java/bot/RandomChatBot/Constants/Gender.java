package bot.RandomChatBot.Constants;

public enum Gender {
    NotStated("Не указан"),
    Girl("Женский"),
    Boy("Мужской");

    private final String rusString;

    Gender(String rusString) {
        this.rusString = rusString;
    }

    public String toRusString() {
        return rusString;
    }
}
