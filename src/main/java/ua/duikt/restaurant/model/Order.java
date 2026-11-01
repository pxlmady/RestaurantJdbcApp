package ua.duikt.restaurant.model;

import java.math.BigDecimal;

/**
 * Рядок таблиці [Order]. reservationId має тип Integer, бо id_reservation допускає NULL
 * (замовлення без бронювання); table_number обов'язковий.
 */
public record Order(int id, Integer reservationId, int waiterId, String paymentStatus,
                    int tableNumber, BigDecimal tips) {

    /** Допустимі значення з CHECK CHK_Order_Status. */
    public static final String[] PAYMENT_STATUSES = {"Оплачено", "В очікуванні", "Скасовано"};
}
