package ua.duikt.restaurant.ui;

import ua.duikt.restaurant.Main;
import ua.duikt.restaurant.dao.CrudDao;

import java.util.List;
import java.util.Optional;

/**
 * Одне меню CRUD для будь-якої таблиці. Знає лише інтерфейс CrudDao
 * і форму сутності (як її вводити й друкувати), але не знає жодного SQL.
 */
public class CrudMenu<T, K> {

    /** Як вводити й показувати конкретну сутність. */
    public interface Form<T, K> {
        List<String> headers();

        List<Object> row(T entity);

        K readKey(ConsoleIO io);

        T readNew(ConsoleIO io);

        /** Повертає змінену копію; Enter у полі залишає старе значення. */
        T edit(ConsoleIO io, T current);
    }

    private final String title;
    private final CrudDao<T, K> dao;
    private final Form<T, K> form;

    public CrudMenu(String title, CrudDao<T, K> dao, Form<T, K> form) {
        this.title = title;
        this.dao = dao;
        this.form = form;
    }

    public void run(ConsoleIO io) {
        while (true) {
            io.out.println();
            io.out.println("=== " + title + " ===");
            io.out.println("1. Переглянути всі   2. Знайти за ключем   3. Додати   4. Редагувати   5. Видалити   0. Назад");
            String choice = io.readLine("> ");
            if (choice.equals("0")) return;
            Main.safely(io, () -> {
                switch (choice) {
                    case "1" -> showAll(io);
                    case "2" -> findOne(io);
                    case "3" -> create(io);
                    case "4" -> update(io);
                    case "5" -> delete(io);
                    default -> io.out.println("Невідомий пункт меню.");
                }
            });
        }
    }

    private void showAll(ConsoleIO io) {
        print(io, dao.findAll());
    }

    private void findOne(ConsoleIO io) {
        K key = form.readKey(io);
        dao.findById(key).ifPresentOrElse(e -> print(io, List.of(e)),
                () -> io.out.println("Запис " + key + " не знайдено."));
    }

    private void create(ConsoleIO io) {
        T entity = form.readNew(io);
        K key = dao.create(entity);
        io.out.println("Додано запис з ключем " + key + ":");
        dao.findById(key).ifPresent(e -> print(io, List.of(e)));
    }

    private void update(ConsoleIO io) {
        K key = form.readKey(io);
        Optional<T> current = dao.findById(key);
        if (current.isEmpty()) {
            io.out.println("Запис " + key + " не знайдено.");
            return;
        }
        print(io, List.of(current.get()));
        T changed = form.edit(io, current.get());
        if (dao.update(changed)) {
            io.out.println("Зміни збережено:");
            dao.findById(key).ifPresent(e -> print(io, List.of(e)));
        } else {
            io.out.println("Запис не змінено (його вже не існує).");
        }
    }

    private void delete(ConsoleIO io) {
        K key = form.readKey(io);
        Optional<T> current = dao.findById(key);
        if (current.isEmpty()) {
            io.out.println("Запис " + key + " не знайдено.");
            return;
        }
        print(io, List.of(current.get()));
        if (!io.confirm("Видалити цей запис?")) {
            io.out.println("Скасовано.");
            return;
        }
        io.out.println(dao.delete(key) ? "Запис видалено." : "Запис не знайдено.");
    }

    private void print(ConsoleIO io, List<T> items) {
        TablePrinter.print(io.out, form.headers(), items.stream().map(form::row).toList());
    }
}
