package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.DataAccessException;
import ua.duikt.restaurant.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Спільна JDBC-частина всіх DAO, щоб не повторювати однаковий код 7 разів.
 * Тут і лише тут створюються PreparedStatement: значення завжди
 * передаються параметрами "?", а не склеюються в рядок SQL,
 * тому SQL-ін'єкція неможлива.
 */
public abstract class BaseDao {

    /** Перетворює поточний рядок ResultSet на об'єкт моделі. */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected final Database db;

    protected BaseDao(Database db) {
        this.db = db;
    }

    /** SELECT, що повертає список рядків. */
    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw DataAccessException.from("Помилка читання даних", e);
        }
    }

    /** SELECT, що повертає не більше одного рядка (пошук за ключем). */
    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        return query(sql, mapper, params).stream().findFirst();
    }

    /** UPDATE або DELETE: повертає кількість змінених рядків. */
    protected int execute(String action, String sql, Object... params) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw DataAccessException.from(action, e);
        }
    }

    /** INSERT у таблицю з IDENTITY: повертає згенерований сервером id. */
    protected int insertReturningId(String action, String sql, Object... params) {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new DataAccessException(action + ": сервер не повернув згенерований ключ");
            }
        } catch (SQLException e) {
            throw DataAccessException.from(action, e);
        }
    }

    /** Підставляє значення в параметри ?, ?, ... по порядку (нумерація з 1). */
    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                ps.setNull(i + 1, Types.INTEGER);   // NULL у нас буває лише в цілих полях (id_reservation)
            } else {
                ps.setObject(i + 1, params[i]);     // драйвер сам обирає тип: Integer, BigDecimal, String, LocalDateTime
            }
        }
    }

    /** getInt повертає 0 замість NULL, тому для полів, що допускають NULL, читаємо Integer. */
    protected static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }
}
