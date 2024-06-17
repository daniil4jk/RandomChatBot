package bot.RandomChatBot;

public enum Gender {
    Girl,
    Boy,
    NotStated;

    public static boolean equals(Gender g, Gender g2) {
        if (g == NotStated) return true;
        return g.equals(g2);
    }

    public static String getRusString(Gender gender) {
        return switch (gender) {
            case Girl -> "Женский";
            case Boy -> "Мужской";
            case NotStated -> "Не указан";
        };
    }
}
