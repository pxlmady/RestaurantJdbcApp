package ua.duikt.restaurant;

import ua.duikt.restaurant.dao.DishDao;
import ua.duikt.restaurant.dao.OrderDao;
import ua.duikt.restaurant.dao.OrderDetailDao;
import ua.duikt.restaurant.dao.ReportDao;
import ua.duikt.restaurant.dao.ReservationDao;
import ua.duikt.restaurant.dao.TableDao;
import ua.duikt.restaurant.dao.VisitorDao;
import ua.duikt.restaurant.dao.WaiterDao;
import ua.duikt.restaurant.db.DataAccessException;
import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.ui.ConsoleIO;
import ua.duikt.restaurant.ui.CrudMenu;
import ua.duikt.restaurant.ui.Forms;
import ua.duikt.restaurant.ui.ReportMenu;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Точка входу: читає налаштування, перевіряє підключення і показує головне меню.
 * Створює DAO та передає їх меню; далі меню працюють лише через DAO.
 */
public class Main {

    public static void main(String[] args) {
        // Кодування консолі: те саме для введення і виводу, щоб кирилиця не ламалась
        // (у IntelliJ це UTF-8, у cmd — кодова сторінка консолі).
        Charset cs = consoleCharset();
        PrintStream out = new PrintStream(System.out, true, cs);
        ConsoleIO io = new ConsoleIO(new Scanner(System.in, cs), out);

        out.println("RestaurantDB — консольний застосунок (Java, JDBC, DAO)");
        Database db;
        try {
            db = Database.fromPropertiesFile();
        } catch (DataAccessException e) {
            out.println("ПОМИЛКА: " + e.getMessage());
            System.exit(1);
            return;
        }
        safely(io, () -> out.println(db.describeConnection()));

        CrudMenu<?, ?>[] menus = {
                new CrudMenu<>("Відвідувачі (Visitor)", new VisitorDao(db), Forms.VISITOR),
                new CrudMenu<>("Столики ([Table])", new TableDao(db), Forms.TABLE),
                new CrudMenu<>("Офіціанти (Waiter)", new WaiterDao(db), Forms.WAITER),
                new CrudMenu<>("Страви (Dish)", new DishDao(db), Forms.DISH),
                new CrudMenu<>("Бронювання (Reservation)", new ReservationDao(db), Forms.RESERVATION),
                new CrudMenu<>("Замовлення ([Order])", new OrderDao(db), Forms.ORDER),
                new CrudMenu<>("Позиції замовлень (OrderDetail)", new OrderDetailDao(db), Forms.ORDER_DETAIL),
        };
        ReportMenu reportMenu = new ReportMenu(new ReportDao(db));

        try {
            while (true) {
                out.println();
                out.println("===== Головне меню =====");
                out.println("1. Відвідувачі   2. Столики   3. Офіціанти   4. Страви");
                out.println("5. Бронювання    6. Замовлення   7. Позиції замовлень");
                out.println("8. Запити (JOIN, фільтрація, агрегація)");
                out.println("9. Перевірити підключення   0. Вихід");
                String choice = io.readLine("> ");
                switch (choice) {
                    case "0" -> {
                        out.println("До побачення!");
                        return;
                    }
                    case "8" -> reportMenu.run(io);
                    case "9" -> safely(io, () -> out.println(db.describeConnection()));
                    default -> {
                        int n = choice.length() == 1 ? choice.charAt(0) - '1' : -1;
                        if (n >= 0 && n < menus.length) menus[n].run(io);
                        else out.println("Невідомий пункт меню.");
                    }
                }
            }
        } catch (ConsoleIO.InputClosedException e) {
            out.println();
            out.println("Введення завершено, вихід.");
        }
    }

    /**
     * Виконує дію і перехоплює помилки БД, щоб програма не падала:
     * користувач бачить пояснення й повертається до меню.
     */
    public static void safely(ConsoleIO io, Runnable action) {
        try {
            action.run();
        } catch (DataAccessException e) {
            io.out.println("ПОМИЛКА: " + e.getMessage());
        } catch (NumberFormatException e) {
            io.out.println("ПОМИЛКА: очікувалося число.");
        }
    }

    private static Charset consoleCharset() {
        String name = System.getProperty("stdout.encoding", System.getProperty("sun.stdout.encoding"));
        try {
            return name != null ? Charset.forName(name) : Charset.defaultCharset();
        } catch (RuntimeException e) {
            return Charset.defaultCharset();
        }
    }
}
