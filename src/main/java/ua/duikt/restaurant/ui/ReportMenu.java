package ua.duikt.restaurant.ui;

import ua.duikt.restaurant.Main;
import ua.duikt.restaurant.dao.ReportDao;
import ua.duikt.restaurant.model.Order;
import ua.duikt.restaurant.model.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Меню обов'язкових запитів: JOIN, фільтрація, агрегатні функції. */
public class ReportMenu {

    private final ReportDao reports;

    public ReportMenu(ReportDao reports) {
        this.reports = reports;
    }

    public void run(ConsoleIO io) {
        while (true) {
            io.out.println();
            io.out.println("=== Запити ===");
            io.out.println("1. [JOIN]      Склад замовлень: клієнт, офіціант, страви, суми");
            io.out.println("2. [Фільтр]    Бронювання за статусом, періодом і кількістю осіб");
            io.out.println("3. [Фільтр]    Страви за назвою і діапазоном цін");
            io.out.println("4. [Агрегація] Показники офіціантів: COUNT, SUM, AVG");
            io.out.println("5. [Агрегація] Популярність страв: COUNT, SUM, HAVING");
            io.out.println("0. Назад");
            String choice = io.readLine("> ");
            if (choice.equals("0")) return;
            Main.safely(io, () -> {
                switch (choice) {
                    case "1" -> orderLines(io);
                    case "2" -> reservations(io);
                    case "3" -> dishes(io);
                    case "4" -> waiterStats(io);
                    case "5" -> dishPopularity(io);
                    default -> io.out.println("Невідомий пункт меню.");
                }
            });
        }
    }

    private void orderLines(ConsoleIO io) {
        Integer orderId = io.readOptionalInt("Номер замовлення (усі — порожньо)");
        List<ReportDao.OrderLine> lines = reports.orderLines(orderId);
        TablePrinter.print(io.out,
                List.of("замовлення", "столик", "клієнт", "офіціант", "страва", "к-сть", "ціна", "сума", "оплата"),
                lines.stream().map(l -> List.<Object>of(l.orderId(), l.tableNumber(), l.visitor(), l.waiter(),
                        l.dish(), l.quantity(), l.price(), l.lineSum(), l.paymentStatus())).toList());
        BigDecimal total = lines.stream().map(ReportDao.OrderLine::lineSum).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!lines.isEmpty()) io.out.println("  Разом за показаними позиціями: " + total);
    }

    private void reservations(ConsoleIO io) {
        String status = io.choose("Статус", Reservation.STATUSES, null, true);
        LocalDate from = io.readOptionalDate("З дати");
        LocalDate to = io.readOptionalDate("По дату включно");
        Integer minPeople = io.readOptionalInt("Мінімум осіб");
        var rows = reports.reservations(status,
                from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(),   // "< наступна доба" включає весь день to
                minPeople);
        TablePrinter.print(io.out, List.of("id", "дата_час", "столик", "відвідувач", "телефон", "осіб", "статус"),
                rows.stream().map(r -> List.<Object>of(r.id(), r.dateTime(), r.tableNumber(), r.visitor(),
                        r.phone(), r.peopleCount(), r.status())).toList());
    }

    private void dishes(ConsoleIO io) {
        String title = io.readOptionalText("Частина назви");
        BigDecimal min = io.readOptionalDecimal("Ціна від");
        BigDecimal max = io.readOptionalDecimal("Ціна до");
        TablePrinter.print(io.out, Forms.DISH.headers(),
                reports.dishes(title, min, max).stream().map(Forms.DISH::row).toList());
    }

    private void waiterStats(ConsoleIO io) {
        String status = io.choose("Рахувати замовлення зі статусом", Order.PAYMENT_STATUSES, "Оплачено", false);
        TablePrinter.print(io.out, List.of("id", "офіціант", "замовлень", "виручка", "середній чек", "чайові"),
                reports.waiterStats(status).stream().map(s -> List.<Object>of(s.waiterId(), s.waiter(),
                        s.ordersCount(), s.revenue(), s.avgCheck(), s.tips())).toList());
    }

    private void dishPopularity(ConsoleIO io) {
        Integer min = io.readOptionalInt("Мінімальна загальна кількість (HAVING)");
        TablePrinter.print(io.out, List.of("id", "страва", "у замовленнях", "порцій", "виручка"),
                reports.dishPopularity(min == null ? 0 : min).stream().map(p -> List.<Object>of(p.dishId(),
                        p.title(), p.ordersCount(), p.totalQuantity(), p.revenue())).toList());
    }
}
