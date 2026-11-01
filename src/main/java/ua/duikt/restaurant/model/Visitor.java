package ua.duikt.restaurant.model;

/** Рядок таблиці Visitor (відвідувач). id = 0 означає "ще не збережено": його видасть IDENTITY. */
public record Visitor(int id, String fullName, String phoneNumber) {
}
