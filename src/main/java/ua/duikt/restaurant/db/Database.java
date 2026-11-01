package ua.duikt.restaurant.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Єдине місце, яке знає, ДЕ лежить база і ЯК до неї підключитися.
 * Параметри читаються з файлу db.properties (url, user, password),
 * тож пароль не зашитий у код.
 * Кожен DAO просить тут нове з'єднання на одну операцію і закриває його
 * через try-with-resources.
 */
public final class Database {

    private final String url;
    private final String user;
    private final String password;

    private Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /** Читає db.properties з поточної папки або зі шляху -Ddb.config=... */
    public static Database fromPropertiesFile() {
        Path path = Path.of(System.getProperty("db.config", "db.properties"));
        if (!Files.exists(path)) {
            throw new DataAccessException("Не знайдено файл налаштувань " + path.toAbsolutePath()
                    + ". Скопіюйте db.properties.example у db.properties і вкажіть пароль.");
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException e) {
            throw new DataAccessException("Не вдалося прочитати " + path + ": " + e.getMessage(), e);
        }
        String url = p.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new DataAccessException("У " + path + " не задано db.url");
        }
        return new Database(url.trim(), p.getProperty("db.user", "").trim(), p.getProperty("db.password", ""));
    }

    /** Нове з'єднання. DriverManager сам знаходить драйвер mssql-jdbc за префіксом jdbc:sqlserver: */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Перевірка підключення: повертає назву бази та версію сервера. */
    public String describeConnection() {
        try (Connection c = getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            return "Підключено: база " + c.getCatalog() + ", " + md.getDatabaseProductName() + " "
                    + md.getDatabaseProductVersion() + ", користувач " + md.getUserName();
        } catch (SQLException e) {
            throw DataAccessException.from("Не вдалося підключитися до SQL Server", e);
        }
    }
}
