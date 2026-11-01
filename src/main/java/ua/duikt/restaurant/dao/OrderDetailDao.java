package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.OrderDetail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** DAO таблиці OrderDetail зі складеним ключем (id_order, id_dish). */
public class OrderDetailDao extends BaseDao implements CrudDao<OrderDetail, OrderDetail.Key> {

    private static final String SELECT = "SELECT id_order, id_dish, quantity FROM OrderDetail";

    public OrderDetailDao(Database db) {
        super(db);
    }

    private static OrderDetail map(ResultSet rs) throws SQLException {
        return new OrderDetail(rs.getInt("id_order"), rs.getInt("id_dish"), rs.getInt("quantity"));
    }

    @Override
    public List<OrderDetail> findAll() {
        return query(SELECT + " ORDER BY id_order, id_dish", OrderDetailDao::map);
    }

    @Override
    public Optional<OrderDetail> findById(OrderDetail.Key key) {
        return queryOne(SELECT + " WHERE id_order = ? AND id_dish = ?", OrderDetailDao::map,
                key.orderId(), key.dishId());
    }

    @Override
    public OrderDetail.Key create(OrderDetail d) {
        execute("Не вдалося додати страву до замовлення",
                "INSERT INTO OrderDetail (id_order, id_dish, quantity) VALUES (?, ?, ?)",
                d.orderId(), d.dishId(), d.quantity());
        return d.key();
    }

    /** Ключ (замовлення + страва) не змінюється, змінюється лише кількість. */
    @Override
    public boolean update(OrderDetail d) {
        return execute("Не вдалося змінити кількість",
                "UPDATE OrderDetail SET quantity = ? WHERE id_order = ? AND id_dish = ?",
                d.quantity(), d.orderId(), d.dishId()) == 1;
    }

    @Override
    public boolean delete(OrderDetail.Key key) {
        return execute("Не вдалося видалити позицію замовлення",
                "DELETE FROM OrderDetail WHERE id_order = ? AND id_dish = ?", key.orderId(), key.dishId()) == 1;
    }
}
