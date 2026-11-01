package ua.duikt.restaurant.ui;

import ua.duikt.restaurant.model.Dish;
import ua.duikt.restaurant.model.Order;
import ua.duikt.restaurant.model.OrderDetail;
import ua.duikt.restaurant.model.Reservation;
import ua.duikt.restaurant.model.RestaurantTable;
import ua.duikt.restaurant.model.Visitor;
import ua.duikt.restaurant.model.Waiter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** Форми введення й колонки виводу для кожної таблиці. Тут немає SQL — лише діалог з користувачем. */
public final class Forms {

    private Forms() {
    }

    public static final CrudMenu.Form<Visitor, Integer> VISITOR = new CrudMenu.Form<>() {
        public List<String> headers() { return List.of("id_visitor", "full_name", "phone_number"); }
        public List<Object> row(Visitor v) { return List.of(v.id(), v.fullName(), v.phoneNumber()); }
        public Integer readKey(ConsoleIO io) { return io.readInt("id_visitor"); }
        public Visitor readNew(ConsoleIO io) {
            return new Visitor(0, io.readText("ПІБ", null), io.readText("Телефон (унікальний)", null));
        }
        public Visitor edit(ConsoleIO io, Visitor v) {
            return new Visitor(v.id(), io.readText("ПІБ", v.fullName()), io.readText("Телефон", v.phoneNumber()));
        }
    };

    public static final CrudMenu.Form<RestaurantTable, Integer> TABLE = new CrudMenu.Form<>() {
        public List<String> headers() { return List.of("table_number", "capacity"); }
        public List<Object> row(RestaurantTable t) { return List.of(t.tableNumber(), t.capacity()); }
        public Integer readKey(ConsoleIO io) { return io.readInt("Номер столика"); }
        public RestaurantTable readNew(ConsoleIO io) {
            return new RestaurantTable(io.readInt("Номер столика"), io.readInt("Місткість (> 0)"));
        }
        public RestaurantTable edit(ConsoleIO io, RestaurantTable t) {
            return new RestaurantTable(t.tableNumber(), io.readInt("Місткість", t.capacity()));
        }
    };

    public static final CrudMenu.Form<Waiter, Integer> WAITER = new CrudMenu.Form<>() {
        public List<String> headers() { return List.of("id_waiter", "full_name"); }
        public List<Object> row(Waiter w) { return List.of(w.id(), w.fullName()); }
        public Integer readKey(ConsoleIO io) { return io.readInt("id_waiter"); }
        public Waiter readNew(ConsoleIO io) { return new Waiter(0, io.readText("ПІБ", null)); }
        public Waiter edit(ConsoleIO io, Waiter w) { return new Waiter(w.id(), io.readText("ПІБ", w.fullName())); }
    };

    public static final CrudMenu.Form<Dish, Integer> DISH = new CrudMenu.Form<>() {
        public List<String> headers() { return List.of("id_dish", "title", "price", "weight_grams"); }
        public List<Object> row(Dish d) { return List.of(d.id(), d.title(), d.price(), d.weightGrams()); }
        public Integer readKey(ConsoleIO io) { return io.readInt("id_dish"); }
        public Dish readNew(ConsoleIO io) {
            return new Dish(0, io.readText("Назва (унікальна)", null), io.readDecimal("Ціна (>= 0)", null),
                    io.readInt("Вага, г (> 0)"));
        }
        public Dish edit(ConsoleIO io, Dish d) {
            return new Dish(d.id(), io.readText("Назва", d.title()), io.readDecimal("Ціна", d.price()),
                    io.readInt("Вага, г", d.weightGrams()));
        }
    };

    public static final CrudMenu.Form<Reservation, Integer> RESERVATION = new CrudMenu.Form<>() {
        public List<String> headers() {
            return List.of("id_reservation", "id_visitor", "table_number", "date_time", "people_count", "status");
        }
        public List<Object> row(Reservation r) {
            return List.of(r.id(), r.visitorId(), r.tableNumber(), r.dateTime(), r.peopleCount(), r.status());
        }
        public Integer readKey(ConsoleIO io) { return io.readInt("id_reservation"); }
        public Reservation readNew(ConsoleIO io) {
            return new Reservation(0, io.readInt("id_visitor"), io.readInt("Номер столика"),
                    io.readDateTime("Дата й час", null), io.readInt("Кількість осіб (> 0)"),
                    io.choose("Статус", Reservation.STATUSES, "Активне", false));
        }
        public Reservation edit(ConsoleIO io, Reservation r) {
            return new Reservation(r.id(), io.readInt("id_visitor", r.visitorId()),
                    io.readInt("Номер столика", r.tableNumber()), io.readDateTime("Дата й час", r.dateTime()),
                    io.readInt("Кількість осіб", r.peopleCount()),
                    io.choose("Статус", Reservation.STATUSES, r.status(), false));
        }
    };

    public static final CrudMenu.Form<Order, Integer> ORDER = new CrudMenu.Form<>() {
        public List<String> headers() {
            return List.of("id_order", "id_reservation", "id_waiter", "payment_status", "table_number", "tips");
        }
        public List<Object> row(Order o) {
            // List.of не приймає null, а id_reservation буває NULL
            return Arrays.asList(o.id(), o.reservationId(), o.waiterId(), o.paymentStatus(), o.tableNumber(), o.tips());
        }
        public Integer readKey(ConsoleIO io) { return io.readInt("id_order"); }
        public Order readNew(ConsoleIO io) {
            Integer reservation = io.readOptionalInt("id_reservation (без бронювання — порожньо)");
            return new Order(0, reservation, io.readInt("id_waiter"),
                    io.choose("Статус оплати", Order.PAYMENT_STATUSES, "В очікуванні", false),
                    io.readInt("Номер столика"), io.readDecimal("Чайові (>= 0)", BigDecimal.ZERO));
        }
        public Order edit(ConsoleIO io, Order o) {
            String r = io.readLine("id_reservation [" + (o.reservationId() == null ? "NULL" : o.reservationId())
                    + "] (Enter — залишити, 0 — прибрати бронювання): ");
            Integer reservation = r.isEmpty() ? o.reservationId() : (r.equals("0") ? null : Integer.valueOf(r));
            return new Order(o.id(), reservation, io.readInt("id_waiter", o.waiterId()),
                    io.choose("Статус оплати", Order.PAYMENT_STATUSES, o.paymentStatus(), false),
                    io.readInt("Номер столика", o.tableNumber()), io.readDecimal("Чайові", o.tips()));
        }
    };

    public static final CrudMenu.Form<OrderDetail, OrderDetail.Key> ORDER_DETAIL = new CrudMenu.Form<>() {
        public List<String> headers() { return List.of("id_order", "id_dish", "quantity"); }
        public List<Object> row(OrderDetail d) { return List.of(d.orderId(), d.dishId(), d.quantity()); }
        public OrderDetail.Key readKey(ConsoleIO io) {
            return new OrderDetail.Key(io.readInt("id_order"), io.readInt("id_dish"));
        }
        public OrderDetail readNew(ConsoleIO io) {
            return new OrderDetail(io.readInt("id_order"), io.readInt("id_dish"), io.readInt("Кількість (> 0)"));
        }
        public OrderDetail edit(ConsoleIO io, OrderDetail d) {
            return new OrderDetail(d.orderId(), d.dishId(), io.readInt("Кількість", d.quantity()));
        }
    };
}
