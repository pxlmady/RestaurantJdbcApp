package ua.duikt.restaurant.dao;

import java.util.List;
import java.util.Optional;

/**
 * Контракт DAO: що можна робити з однією таблицею.
 * Інтерфейс користувача працює лише з цими методами й не бачить SQL.
 *
 * @param <T> модель (рядок таблиці)
 * @param <K> тип первинного ключа
 */
public interface CrudDao<T, K> {

    List<T> findAll();                 // Read: усі записи

    Optional<T> findById(K id);        // Read: один запис за ключем

    K create(T entity);                // Create: повертає ключ нового запису

    boolean update(T entity);          // Update: true, якщо рядок знайдено й змінено

    boolean delete(K id);              // Delete: true, якщо рядок видалено
}
