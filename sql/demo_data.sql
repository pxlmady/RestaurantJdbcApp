/* =====================================================================
   НЕОБОВ'ЯЗКОВО. Демо-дані для показу запитів ПР 3.1(5) у RestaurantDB.
   Лише ДОДАЄ рядки (INSERT), структуру не змінює, наявні дані не чіпає.
   Повторний запуск нічого не дублює: кожен INSERT перевіряє NOT EXISTS.
   Усі демо-замовлення мають бронювання, бо UQ_Order_Reservation
   дозволяє лише один NULL в id_reservation, а він уже зайнятий замовленням 2.
   ===================================================================== */
USE RestaurantDB;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

INSERT INTO Visitor (full_name, phone_number)
SELECT v.full_name, v.phone FROM (VALUES
    (N'Коваленко Марія', '+380501234501'),
    (N'Шевчук Андрій',   '+380501234502'),
    (N'Бондар Олена',    '+380501234503')) v(full_name, phone)
WHERE NOT EXISTS (SELECT 1 FROM Visitor x WHERE x.phone_number = v.phone);

INSERT INTO [Table] (table_number, capacity)
SELECT t.n, t.c FROM (VALUES (3, 2), (4, 6), (5, 8)) t(n, c)
WHERE NOT EXISTS (SELECT 1 FROM [Table] x WHERE x.table_number = t.n);

INSERT INTO Waiter (full_name)
SELECT w.n FROM (VALUES (N'Ткаченко Ірина'), (N'Лисенко Дмитро')) w(n)
WHERE NOT EXISTS (SELECT 1 FROM Waiter x WHERE x.full_name = w.n);

INSERT INTO Dish (title, price, weight_grams)
SELECT d.t, d.p, d.w FROM (VALUES
    (N'Вареники з картоплею', 120.00, 300),
    (N'Деруни зі сметаною',   135.00, 280),
    (N'Котлета по-київськи',  210.00, 250),
    (N'Узвар',                 45.00, 300),
    (N'Сирники',              110.00, 220)) d(t, p, w)
WHERE NOT EXISTS (SELECT 1 FROM Dish x WHERE x.title = d.t);

-- Бронювання: відвідувача шукаємо за телефоном, тому id не захардкоджені
INSERT INTO Reservation (id_visitor, table_number, date_time, people_count, reservation_status)
SELECT v.id_visitor, r.tbl, r.dt, r.ppl, r.st FROM (VALUES
    ('+380501234501', 3, CAST('2026-10-25 19:00' AS DATETIME), 2, N'Завершене'),
    ('+380501234502', 4, CAST('2026-10-26 20:00' AS DATETIME), 5, N'Завершене'),
    ('+380501234503', 5, CAST('2026-11-02 18:30' AS DATETIME), 7, N'Активне'),
    ('+380501234501', 4, CAST('2026-11-07 13:00' AS DATETIME), 4, N'Скасоване')) r(phone, tbl, dt, ppl, st)
JOIN Visitor v ON v.phone_number = r.phone
WHERE NOT EXISTS (SELECT 1 FROM Reservation x WHERE x.id_visitor = v.id_visitor AND x.date_time = r.dt);

-- Замовлення до двох завершених бронювань і одного активного
INSERT INTO [Order] (id_reservation, id_waiter, payment_status, table_number, tips)
SELECT res.id_reservation, w.id_waiter, o.st, res.table_number, o.tips FROM (VALUES
    ('+380501234501', CAST('2026-10-25 19:00' AS DATETIME), N'Ткаченко Ірина', N'Оплачено',     50.00),
    ('+380501234502', CAST('2026-10-26 20:00' AS DATETIME), N'Лисенко Дмитро', N'Оплачено',    120.00),
    ('+380501234503', CAST('2026-11-02 18:30' AS DATETIME), N'Ткаченко Ірина', N'В очікуванні',  0.00)) o(phone, dt, waiter, st, tips)
JOIN Visitor v      ON v.phone_number = o.phone
JOIN Reservation res ON res.id_visitor = v.id_visitor AND res.date_time = o.dt
JOIN Waiter w       ON w.full_name = o.waiter
WHERE NOT EXISTS (SELECT 1 FROM [Order] x WHERE x.id_reservation = res.id_reservation);

INSERT INTO OrderDetail (id_order, id_dish, quantity)
SELECT ord.id_order, d.id_dish, i.qty FROM (VALUES
    ('+380501234501', CAST('2026-10-25 19:00' AS DATETIME), N'Борщ',                 2),
    ('+380501234501', CAST('2026-10-25 19:00' AS DATETIME), N'Вареники з картоплею', 2),
    ('+380501234501', CAST('2026-10-25 19:00' AS DATETIME), N'Узвар',                2),
    ('+380501234502', CAST('2026-10-26 20:00' AS DATETIME), N'Котлета по-київськи',  5),
    ('+380501234502', CAST('2026-10-26 20:00' AS DATETIME), N'Деруни зі сметаною',   3),
    ('+380501234502', CAST('2026-10-26 20:00' AS DATETIME), N'Узвар',                5),
    ('+380501234503', CAST('2026-11-02 18:30' AS DATETIME), N'Сирники',              4),
    ('+380501234503', CAST('2026-11-02 18:30' AS DATETIME), N'Борщ',                 3)) i(phone, dt, dish, qty)
JOIN Visitor v       ON v.phone_number = i.phone
JOIN Reservation res ON res.id_visitor = v.id_visitor AND res.date_time = i.dt
JOIN [Order] ord     ON ord.id_reservation = res.id_reservation
JOIN Dish d          ON d.title = i.dish
WHERE NOT EXISTS (SELECT 1 FROM OrderDetail x WHERE x.id_order = ord.id_order AND x.id_dish = d.id_dish);

COMMIT;
GO
SELECT 'Visitor' AS tbl, COUNT(*) AS rows FROM Visitor UNION ALL
SELECT '[Table]', COUNT(*) FROM [Table] UNION ALL
SELECT 'Waiter', COUNT(*) FROM Waiter UNION ALL
SELECT 'Dish', COUNT(*) FROM Dish UNION ALL
SELECT 'Reservation', COUNT(*) FROM Reservation UNION ALL
SELECT '[Order]', COUNT(*) FROM [Order] UNION ALL
SELECT 'OrderDetail', COUNT(*) FROM OrderDetail;
GO
