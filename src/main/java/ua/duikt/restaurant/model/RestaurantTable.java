package ua.duikt.restaurant.model;

/**
 * Рядок таблиці [Table] (столик). Назва класу не Table, щоб не плутати
 * зі словом TABLE в SQL. Ключ table_number задається вручну (не IDENTITY).
 */
public record RestaurantTable(int tableNumber, int capacity) {
}
