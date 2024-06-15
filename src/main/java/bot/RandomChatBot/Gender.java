package bot.RandomChatBot;

public enum Gender {
    Female,
    Male,
    NotStated;

    public static boolean equals(Gender g, Gender g2) {
        if (g == NotStated) return true;
        return g.equals(g2);
    }

    public static String formatToRusString(Gender gender) {
        return switch (gender) {
            case Female -> "Женский";
            case Male -> "Мужской";
            case NotStated -> "Не указан";
        };
    }
}
