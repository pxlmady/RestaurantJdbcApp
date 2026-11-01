package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.RestaurantTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO таблиці [Table]. Назва таблиці збігається з ключовим словом SQL,
 * тому в запитах вона завжди в квадратних дужках.
 * Номер столика вводить користувач, тому create повертає його ж, а не IDENTITY.
 */
public class TableDao extends BaseDao implements CrudDao<RestaurantTable, Integer> {

    public TableDao(Database db) {
        super(db);
    }

    private static RestaurantTable map(ResultSet rs) throws SQLException {
        return new RestaurantTable(rs.getInt("table_number"), rs.getInt("capacity"));
    }

    @Override
    public List<RestaurantTable> findAll() {
        return query("SELECT table_number, capacity FROM [Table] ORDER BY table_number", TableDao::map);
    }

    @Override
    public Optional<RestaurantTable> findById(Integer number) {
        return queryOne("SELECT table_number, capacity FROM [Table] WHERE table_number = ?", TableDao::map, number);
    }

    @Override
    public Integer create(RestaurantTable t) {
        execute("Не вдалося додати столик",
                "INSERT INTO [Table] (table_number, capacity) VALUES (?, ?)", t.tableNumber(), t.capacity());
        return t.tableNumber();
    }

    @Override
    public boolean update(RestaurantTable t) {
        return execute("Не вдалося змінити столик",
                "UPDATE [Table] SET capacity = ? WHERE table_number = ?", t.capacity(), t.tableNumber()) == 1;
    }

    @Override
    public boolean delete(Integer number) {
        return execute("Не вдалося видалити столик",
                "DELETE FROM [Table] WHERE table_number = ?", number) == 1;
    }
}
