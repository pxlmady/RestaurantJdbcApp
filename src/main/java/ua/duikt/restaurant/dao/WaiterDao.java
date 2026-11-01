package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.Waiter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** DAO таблиці Waiter. */
public class WaiterDao extends BaseDao implements CrudDao<Waiter, Integer> {

    public WaiterDao(Database db) {
        super(db);
    }

    private static Waiter map(ResultSet rs) throws SQLException {
        return new Waiter(rs.getInt("id_waiter"), rs.getString("full_name"));
    }

    @Override
    public List<Waiter> findAll() {
        return query("SELECT id_waiter, full_name FROM Waiter ORDER BY id_waiter", WaiterDao::map);
    }

    @Override
    public Optional<Waiter> findById(Integer id) {
        return queryOne("SELECT id_waiter, full_name FROM Waiter WHERE id_waiter = ?", WaiterDao::map, id);
    }

    @Override
    public Integer create(Waiter w) {
        return insertReturningId("Не вдалося додати офіціанта",
                "INSERT INTO Waiter (full_name) VALUES (?)", w.fullName());
    }

    @Override
    public boolean update(Waiter w) {
        return execute("Не вдалося змінити офіціанта",
                "UPDATE Waiter SET full_name = ? WHERE id_waiter = ?", w.fullName(), w.id()) == 1;
    }

    @Override
    public boolean delete(Integer id) {
        return execute("Не вдалося видалити офіціанта",
                "DELETE FROM Waiter WHERE id_waiter = ?", id) == 1;
    }
}
