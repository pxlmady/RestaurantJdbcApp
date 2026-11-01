package ua.duikt.restaurant.model;

import java.math.BigDecimal;

/** Рядок таблиці Dish (страва). price DECIMAL(10,2) у Java відповідає BigDecimal, а не double. */
public record Dish(int id, String title, BigDecimal price, int weightGrams) {
}
