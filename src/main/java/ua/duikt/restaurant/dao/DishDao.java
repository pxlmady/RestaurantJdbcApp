package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.Dish;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** DAO таблиці Dish. */
public class DishDao extends BaseDao implements CrudDao<Dish, Integer> {

    private static final String SELECT = "SELECT id_dish, title, price, weight_grams FROM Dish";

    public DishDao(Database db) {
        super(db);
    }

    static Dish map(ResultSet rs) throws SQLException {
        return new Dish(rs.getInt("id_dish"), rs.getString("title"), rs.getBigDecimal("price"),
                rs.getInt("weight_grams"));
    }

    @Override
    public List<Dish> findAll() {
        return query(SELECT + " ORDER BY id_dish", DishDao::map);
    }

    @Override
    public Optional<Dish> findById(Integer id) {
        return queryOne(SELECT + " WHERE id_dish = ?", DishDao::map, id);
    }

    @Override
    public Integer create(Dish d) {
        return insertReturningId("Не вдалося додати страву",
                "INSERT INTO Dish (title, price, weight_grams) VALUES (?, ?, ?)",
                d.title(), d.price(), d.weightGrams());
    }

    @Override
    public boolean update(Dish d) {
        return execute("Не вдалося змінити страву",
                "UPDATE Dish SET title = ?, price = ?, weight_grams = ? WHERE id_dish = ?",
                d.title(), d.price(), d.weightGrams(), d.id()) == 1;
    }

    @Override
    public boolean delete(Integer id) {
        return execute("Не вдалося видалити страву", "DELETE FROM Dish WHERE id_dish = ?", id) == 1;
    }
}
