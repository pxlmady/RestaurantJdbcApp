package ua.duikt.restaurant.dao;

import ua.duikt.restaurant.db.Database;
import ua.duikt.restaurant.model.Visitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** DAO таблиці Visitor: увесь SQL для відвідувачів зібраний тут. */
public class VisitorDao extends BaseDao implements CrudDao<Visitor, Integer> {

    public VisitorDao(Database db) {
        super(db);
    }

    private static Visitor map(ResultSet rs) throws SQLException {
        return new Visitor(rs.getInt("id_visitor"), rs.getString("full_name"), rs.getString("phone_number"));
    }

    @Override
    public List<Visitor> findAll() {
        return query("SELECT id_visitor, full_name, phone_number FROM Visitor ORDER BY id_visitor",
                VisitorDao::map);
    }

    @Override
    public Optional<Visitor> findById(Integer id) {
        return queryOne("SELECT id_visitor, full_name, phone_number FROM Visitor WHERE id_visitor = ?",
                VisitorDao::map, id);
    }

    @Override
    public Integer create(Visitor v) {
        return insertReturningId("Не вдалося додати відвідувача",
                "INSERT INTO Visitor (full_name, phone_number) VALUES (?, ?)",
                v.fullName(), v.phoneNumber());
    }

    @Override
    public boolean update(Visitor v) {
        return execute("Не вдалося змінити відвідувача",
                "UPDATE Visitor SET full_name = ?, phone_number = ? WHERE id_visitor = ?",
                v.fullName(), v.phoneNumber(), v.id()) == 1;
    }

    @Override
    public boolean delete(Integer id) {
        return execute("Не вдалося видалити відвідувача",
                "DELETE FROM Visitor WHERE id_visitor = ?", id) == 1;
    }
}
