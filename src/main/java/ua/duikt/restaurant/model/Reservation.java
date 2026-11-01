package ua.duikt.restaurant.model;

import java.time.LocalDateTime;

/** Рядок таблиці Reservation. id_visitor і table_number є зовнішніми ключами. */
public record Reservation(int id, int visitorId, int tableNumber, LocalDateTime dateTime,
                          int peopleCount, String status) {

    /** Допустимі значення з CHECK CHK_Reservation_Status. */
    public static final String[] STATUSES = {"Активне", "Скасоване", "Завершене"};
}
