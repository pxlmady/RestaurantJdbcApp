package ua.duikt.restaurant.db;

import java.sql.SQLException;

/**
 * Помилка роботи з БД, зрозуміла користувачу.
 * DAO перехоплює SQLException і перетворює її на це виключення,
 * тому консольний інтерфейс не залежить від JDBC.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Перекладає найчастіші коди помилок SQL Server у зрозумілі повідомлення. */
    public static DataAccessException from(String action, SQLException e) {
        String reason = switch (e.getErrorCode()) {
            // 547 — порушення FOREIGN KEY або CHECK; розрізняємо за текстом повідомлення
            case 547 -> e.getMessage().contains("REFERENCE")
                    ? "на цей запис посилаються записи інших таблиць (FOREIGN KEY), спершу видаліть або змініть їх"
                    : e.getMessage().contains("FOREIGN KEY")
                    ? "вказаний ключ не існує у пов'язаній таблиці (FOREIGN KEY)"
                    : "значення не пройшло перевірку CHECK";
            case 2627, 2601 -> e.getMessage().contains("(<NULL>)")
                    ? "у стовпці з UNIQUE вже є NULL, а обмеження UNIQUE дозволяє лише один NULL"
                    : "такий ключ або унікальне значення вже існує";
            case 515 -> "обов'язкове поле не заповнене (NULL)";
            case 18456 -> "невірний логін або пароль";
            case 4060 -> "базу даних не знайдено або немає доступу";
            default -> null;
        };
        if (reason == null && "08S01".equals(e.getSQLState())) {
            reason = "сервер недоступний (перевірте, що SQL Server запущений і порт у db.properties правильний)";
        }
        String text = action + ": " + (reason != null ? reason : "помилка SQL Server")
                + "\n   [SQL Server " + e.getErrorCode() + "] " + e.getMessage();
        return new DataAccessException(text, e);
    }
}
