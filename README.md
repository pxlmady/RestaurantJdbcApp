# RestaurantDB: консольний застосунок на Java (JDBC + DAO), ПР 3.1(5)

Підключається до RestaurantDB на SQL Server через JDBC-драйвер Microsoft (`mssql-jdbc`),
дає CRUD для 7 таблиць і 5 запитів (JOIN, фільтрація, агрегатні функції). ORM не використовується.

## Запуск

1. Вимоги: JDK 17+, IntelliJ IDEA (Maven вбудований), запущений SQL Server з базою RestaurantDB.
2. Скопіюйте `db.properties.example` → `db.properties` і вкажіть `db.password` (і порт, якщо інший).
3. IntelliJ IDEA: **File → Open** → папка з `pom.xml` → дочекатися імпорту Maven →
   відкрити `src/main/java/ua/duikt/restaurant/Main.java` → зелений ▶ біля `main`.
   Робоча папка конфігурації запуску має бути коренем проєкту (так за замовчуванням), бо там лежить `db.properties`.
4. Або з терміналу в корені проєкту: `mvn -q compile exec:java`.

## Структура

```
src/main/java/ua/duikt/restaurant/
  Main.java            головне меню, обробка помилок
  db/Database.java     читає db.properties, видає Connection (DriverManager)
  db/DataAccessException.java  зрозумілі повідомлення замість SQLException
  model/               record-и: рядки таблиць
  dao/CrudDao.java     інтерфейс DAO: findAll, findById, create, update, delete
  dao/BaseDao.java     спільний JDBC-код з PreparedStatement
  dao/*Dao.java        SQL для кожної таблиці
  dao/ReportDao.java   5 запитів-звітів
  ui/                  консольні меню й форми, без SQL
sql/demo_data.sql      НЕОБОВ'ЯЗКОВІ демо-дані (лише INSERT, повторний запуск не дублює)
```

Детальне пояснення та сценарій захисту: `ПР3.1(5)_Пояснення_і_захист.md`.
