package ua.duikt.restaurant.model;

/** Рядок таблиці OrderDetail: страва в замовленні. Первинний ключ складений (id_order, id_dish). */
public record OrderDetail(int orderId, int dishId, int quantity) {

    /** Складений ключ як один об'єкт, щоб OrderDetailDao теж реалізовував CrudDao. */
    public record Key(int orderId, int dishId) {
    }

    public Key key() {
        return new Key(orderId, dishId);
    }
}
