package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.Reservation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** DAO таблиці Reservation. */
public class ReservationDao extends BaseDao implements CrudDao<Reservation, Integer> {

    private static final String SELECT =
            "SELECT id_reservation, id_visitor, table_number, date_time, people_count, reservation_status FROM Reservation";

    public ReservationDao(Database db) {
        super(db);
    }

    private static Reservation map(ResultSet rs) throws SQLException {
        return new Reservation(rs.getInt("id_reservation"), rs.getInt("id_visitor"), rs.getInt("table_number"),
                rs.getObject("date_time", LocalDateTime.class), rs.getInt("people_count"),
                rs.getString("reservation_status"));
    }

    @Override
    public List<Reservation> findAll() {
        return query(SELECT + " ORDER BY date_time, id_reservation", ReservationDao::map);
    }

    @Override
    public Optional<Reservation> findById(Integer id) {
        return queryOne(SELECT + " WHERE id_reservation = ?", ReservationDao::map, id);
    }

    @Override
    public Integer create(Reservation r) {
        return insertReturningId("Не вдалося додати бронювання",
                "INSERT INTO Reservation (id_visitor, table_number, date_time, people_count, reservation_status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                r.visitorId(), r.tableNumber(), r.dateTime(), r.peopleCount(), r.status());
    }

    @Override
    public boolean update(Reservation r) {
        return execute("Не вдалося змінити бронювання",
                "UPDATE Reservation SET id_visitor = ?, table_number = ?, date_time = ?, people_count = ?, "
                        + "reservation_status = ? WHERE id_reservation = ?",
                r.visitorId(), r.tableNumber(), r.dateTime(), r.peopleCount(), r.status(), r.id()) == 1;
    }

    @Override
    public boolean delete(Integer id) {
        return execute("Не вдалося видалити бронювання",
                "DELETE FROM Reservation WHERE id_reservation = ?", id) == 1;
    }
}
