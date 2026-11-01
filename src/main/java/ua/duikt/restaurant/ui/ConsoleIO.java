package ua.duikt.restaurant.ui;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Введення з консолі з перевіркою: при помилці формату питання повторюється.
 * Методи з параметром current використовуються під час редагування:
 * порожній Enter залишає поточне значення.
 */
public class ConsoleIO {

    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Кінець введення (Ctrl+D / закритий потік) — застосунок завершується штатно. */
    public static class InputClosedException extends RuntimeException {
    }

    private final Scanner in;
    public final PrintStream out;

    public ConsoleIO(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        if (!in.hasNextLine()) {
            throw new InputClosedException();
        }
        return in.nextLine().trim();
    }

    /** Обов'язковий текст; при редагуванні Enter = залишити current. */
    public String readText(String prompt, String current) {
        while (true) {
            String s = readLine(prompt + hint(current) + ": ");
            if (!s.isEmpty()) return s;
            if (current != null) return current;
            out.println("  Поле обов'язкове.");
        }
    }

    /** Текст, який можна пропустити (null). */
    public String readOptionalText(String prompt) {
        String s = readLine(prompt + " [Enter — пропустити]: ");
        return s.isEmpty() ? null : s;
    }

    public int readInt(String prompt) {
        return readInt(prompt, null);
    }

    public int readInt(String prompt, Integer current) {
        while (true) {
            String s = readLine(prompt + hint(current) + ": ");
            if (s.isEmpty() && current != null) return current;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                out.println("  Потрібне ціле число.");
            }
        }
    }

    /** Ціле число або null (порожній рядок). */
    public Integer readOptionalInt(String prompt) {
        while (true) {
            String s = readLine(prompt + " [Enter — пропустити]: ");
            if (s.isEmpty()) return null;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                out.println("  Потрібне ціле число або порожній рядок.");
            }
        }
    }

    public BigDecimal readDecimal(String prompt, BigDecimal current) {
        while (true) {
            String s = readLine(prompt + hint(current) + ": ");
            if (s.isEmpty() && current != null) return current;
            try {
                return new BigDecimal(s.replace(',', '.'));
            } catch (NumberFormatException e) {
                out.println("  Потрібне число, наприклад 140.50");
            }
        }
    }

    public BigDecimal readOptionalDecimal(String prompt) {
        while (true) {
            String s = readLine(prompt + " [Enter — пропустити]: ");
            if (s.isEmpty()) return null;
            try {
                return new BigDecimal(s.replace(',', '.'));
            } catch (NumberFormatException e) {
                out.println("  Потрібне число або порожній рядок.");
            }
        }
    }

    public LocalDateTime readDateTime(String prompt, LocalDateTime current) {
        while (true) {
            String s = readLine(prompt + " (рррр-мм-дд гг:хх)" + hint(current == null ? null : DATE_TIME.format(current)) + ": ");
            if (s.isEmpty() && current != null) return current;
            try {
                return LocalDateTime.parse(s, DATE_TIME);
            } catch (DateTimeParseException e) {
                out.println("  Формат: 2026-10-28 19:30");
            }
        }
    }

    /** Дата без часу або null; повертає початок доби. */
    public LocalDate readOptionalDate(String prompt) {
        while (true) {
            String s = readLine(prompt + " (рррр-мм-дд) [Enter — пропустити]: ");
            if (s.isEmpty()) return null;
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                out.println("  Формат: 2026-10-28");
            }
        }
    }

    /** Вибір значення зі списку допустимих (значення з CHECK-обмежень). */
    public String choose(String prompt, String[] options, String current, boolean allowSkip) {
        StringBuilder sb = new StringBuilder(prompt).append(" (");
        for (int i = 0; i < options.length; i++) {
            sb.append(i + 1).append(" — ").append(options[i]).append(i < options.length - 1 ? ", " : ")");
        }
        if (current != null) sb.append(" [Enter — ").append(current).append("]");
        else if (allowSkip) sb.append(" [Enter — будь-який]");
        while (true) {
            String s = readLine(sb + ": ");
            if (s.isEmpty() && (current != null || allowSkip)) return current;
            try {
                int n = Integer.parseInt(s);
                if (n >= 1 && n <= options.length) return options[n - 1];
            } catch (NumberFormatException ignored) {
                // повторюємо питання
            }
            out.println("  Оберіть номер від 1 до " + options.length + ".");
        }
    }

    public boolean confirm(String prompt) {
        String s = readLine(prompt + " (так/ні): ").toLowerCase();
        return s.equals("так") || s.equals("т") || s.equals("y") || s.equals("yes");
    }

    private static String hint(Object current) {
        return current == null ? "" : " [" + current + "]";
    }
}
