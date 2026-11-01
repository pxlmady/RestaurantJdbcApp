package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO таблиці [Order]. Видалення замовлення каскадно видаляє його позиції
 * (FK_OrderDetail_Order ON DELETE CASCADE) — це робить сам SQL Server.
 */
public class OrderDao extends BaseDao implements CrudDao<Order, Integer> {

    private static final String SELECT =
            "SELECT id_order, id_reservation, id_waiter, payment_status, table_number, tips FROM [Order]";

    public OrderDao(Database db) {
        super(db);
    }

    private static Order map(ResultSet rs) throws SQLException {
        return new Order(rs.getInt("id_order"), getNullableInt(rs, "id_reservation"), rs.getInt("id_waiter"),
                rs.getString("payment_status"), rs.getInt("table_number"), rs.getBigDecimal("tips"));
    }

    @Override
    public List<Order> findAll() {
        return query(SELECT + " ORDER BY id_order", OrderDao::map);
    }

    @Override
    public Optional<Order> findById(Integer id) {
        return queryOne(SELECT + " WHERE id_order = ?", OrderDao::map, id);
    }

    @Override
    public Integer create(Order o) {
        return insertReturningId("Не вдалося додати замовлення",
                "INSERT INTO [Order] (id_reservation, id_waiter, payment_status, table_number, tips) "
                        + "VALUES (?, ?, ?, ?, ?)",
                o.reservationId(), o.waiterId(), o.paymentStatus(), o.tableNumber(), o.tips());
    }

    @Override
    public boolean update(Order o) {
        return execute("Не вдалося змінити замовлення",
                "UPDATE [Order] SET id_reservation = ?, id_waiter = ?, payment_status = ?, table_number = ?, "
                        + "tips = ? WHERE id_order = ?",
                o.reservationId(), o.waiterId(), o.paymentStatus(), o.tableNumber(), o.tips(), o.id()) == 1;
    }

    @Override
    public boolean delete(Integer id) {
        return execute("Не вдалося видалити замовлення", "DELETE FROM [Order] WHERE id_order = ?", id) == 1;
    }
}
