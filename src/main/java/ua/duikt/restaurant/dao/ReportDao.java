package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.Dish;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для запитів-звітів, що читають кілька таблиць одразу:
 * JOIN, фільтрація з параметрами, агрегатні функції з GROUP BY.
 * Кожен метод повертає список простих record-ів, які консоль друкує таблицею.
 */
public class ReportDao extends BaseDao {

    public ReportDao(Database db) {
        super(db);
    }

    // ---------- 1. JOIN: склад замовлень ----------

    public record OrderLine(int orderId, int tableNumber, String visitor, String waiter, String dish,
                            int quantity, BigDecimal price, BigDecimal lineSum, String paymentStatus) {
    }

    /**
     * Позиції замовлень разом з офіціантом, стравою і клієнтом.
     * INNER JOIN для обов'язкових зв'язків, LEFT JOIN для бронювання,
     * бо id_reservation може бути NULL (замовлення без бронювання не має відвідувача).
     *
     * @param orderId номер замовлення або null — усі замовлення
     */
    public List<OrderLine> orderLines(Integer orderId) {
        String sql = """
                SELECT o.id_order, o.table_number,
                       COALESCE(v.full_name, N'(без бронювання)') AS visitor,
                       w.full_name AS waiter, d.title, od.quantity, d.price,
                       od.quantity * d.price AS line_sum, o.payment_status
                FROM [Order] o
                JOIN Waiter w       ON w.id_waiter = o.id_waiter
                JOIN OrderDetail od ON od.id_order = o.id_order
                JOIN Dish d         ON d.id_dish = od.id_dish
                LEFT JOIN Reservation r ON r.id_reservation = o.id_reservation
                LEFT JOIN Visitor v     ON v.id_visitor = r.id_visitor
                """;
        List<Object> params = new ArrayList<>();
        if (orderId != null) {
            sql += "WHERE o.id_order = ?\n";
            params.add(orderId);
        }
        sql += "ORDER BY o.id_order, d.title";
        return query(sql, rs -> new OrderLine(rs.getInt("id_order"), rs.getInt("table_number"),
                rs.getString("visitor"), rs.getString("waiter"), rs.getString("title"), rs.getInt("quantity"),
                rs.getBigDecimal("price"), rs.getBigDecimal("line_sum"), rs.getString("payment_status")),
                params.toArray());
    }

    // ---------- 2. Фільтрація: бронювання за статусом і періодом ----------

    public record ReservationRow(int id, LocalDateTime dateTime, int tableNumber, String visitor,
                                 String phone, int peopleCount, String status) {
    }

    /**
     * Бронювання з умовами WHERE, які задає користувач. Будь-яку умову можна пропустити (null).
     * Текст SQL складається лише з наших власних фрагментів "AND ... = ?",
     * а значення користувача йдуть тільки в параметри PreparedStatement.
     */
    public List<ReservationRow> reservations(String status, LocalDateTime from, LocalDateTime to, Integer minPeople) {
        StringBuilder sql = new StringBuilder("""
                SELECT r.id_reservation, r.date_time, r.table_number, v.full_name, v.phone_number,
                       r.people_count, r.reservation_status
                FROM Reservation r
                JOIN Visitor v ON v.id_visitor = r.id_visitor
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append(" AND r.reservation_status = ?");
            params.add(status);
        }
        if (from != null) {
            sql.append(" AND r.date_time >= ?");
            params.add(from);
        }
        if (to != null) {
            sql.append(" AND r.date_time < ?");
            params.add(to);
        }
        if (minPeople != null) {
            sql.append(" AND r.people_count >= ?");
            params.add(minPeople);
        }
        sql.append(" ORDER BY r.date_time");
        return query(sql.toString(), rs -> new ReservationRow(rs.getInt("id_reservation"),
                rs.getObject("date_time", LocalDateTime.class), rs.getInt("table_number"),
                rs.getString("full_name"), rs.getString("phone_number"), rs.getInt("people_count"),
                rs.getString("reservation_status")), params.toArray());
    }

    // ---------- 3. Фільтрація: страви за назвою і ціною ----------

    /** LIKE з параметром: символи % додаються в Java, тому введений текст не може змінити запит. */
    public List<Dish> dishes(String titlePart, BigDecimal minPrice, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT id_dish, title, price, weight_grams FROM Dish WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (titlePart != null) {
            sql.append(" AND title LIKE ?");
            params.add("%" + titlePart + "%");
        }
        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }
        sql.append(" ORDER BY price, title");
        return query(sql.toString(), DishDao::map, params.toArray());
    }

    // ---------- 4. Агрегація: показники офіціантів ----------

    public record WaiterStats(int waiterId, String waiter, int ordersCount, BigDecimal revenue,
                              BigDecimal avgCheck, BigDecimal tips) {
    }

    /**
     * COUNT / SUM / AVG по кожному офіціанту для замовлень з обраним статусом оплати.
     * Сума кожного замовлення спершу рахується у підзапиті t (SUM по його позиціях),
     * інакше чайові o.tips додалися б стільки разів, скільки страв у замовленні.
     * LEFT JOIN залишає в звіті офіціантів без замовлень (з нулями).
     */
    public List<WaiterStats> waiterStats(String paymentStatus) {
        String sql = """
                SELECT w.id_waiter, w.full_name,
                       COUNT(o.id_order)              AS orders_count,
                       COALESCE(SUM(t.order_sum), 0)  AS revenue,
                       COALESCE(AVG(t.order_sum), 0)  AS avg_check,
                       COALESCE(SUM(o.tips), 0)       AS tips_total
                FROM Waiter w
                LEFT JOIN [Order] o ON o.id_waiter = w.id_waiter AND o.payment_status = ?
                LEFT JOIN (SELECT od.id_order, SUM(od.quantity * d.price) AS order_sum
                           FROM OrderDetail od
                           JOIN Dish d ON d.id_dish = od.id_dish
                           GROUP BY od.id_order) t ON t.id_order = o.id_order
                GROUP BY w.id_waiter, w.full_name
                ORDER BY revenue DESC, w.full_name
                """;
        return query(sql, rs -> new WaiterStats(rs.getInt("id_waiter"), rs.getString("full_name"),
                rs.getInt("orders_count"), rs.getBigDecimal("revenue"), rs.getBigDecimal("avg_check"),
                rs.getBigDecimal("tips_total")), paymentStatus);
    }

    // ---------- 5. Агрегація: популярність страв ----------

    public record DishPopularity(int dishId, String title, int ordersCount, int totalQuantity, BigDecimal revenue) {
    }

    /** Скільки разів і в якій кількості замовляли кожну страву; HAVING відсікає страви з меншою кількістю. */
    public List<DishPopularity> dishPopularity(int minQuantity) {
        String sql = """
                SELECT d.id_dish, d.title,
                       COUNT(od.id_order)                      AS orders_count,
                       COALESCE(SUM(od.quantity), 0)           AS total_qty,
                       COALESCE(SUM(od.quantity * d.price), 0) AS revenue
                FROM Dish d
                LEFT JOIN OrderDetail od ON od.id_dish = d.id_dish
                GROUP BY d.id_dish, d.title
                HAVING COALESCE(SUM(od.quantity), 0) >= ?
                ORDER BY total_qty DESC, d.title
                """;
        return query(sql, rs -> new DishPopularity(rs.getInt("id_dish"), rs.getString("title"),
                rs.getInt("orders_count"), rs.getInt("total_qty"), rs.getBigDecimal("revenue")), minQuantity);
    }
}
