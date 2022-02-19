package Chat;

/**
 * 1. Добавить в сетевой чат запись локальной истории в текстовый файл на клиенте.
 * 2. После загрузки клиента показывать ему последние 100 строк чата.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Optional;


public class SQLiteDBAuthService implements AuthService {

    private static Connection connection; // интерфейс для соединения с базой данных
    private static Statement statement; // интерфейс для отправки запросов в БД
    private static final Logger LOGGER = LogManager.getLogger(SQLiteDBAuthService.class);

    @Override
    public void start() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:users.db");
        statement = connection.createStatement();
        createTable();
        insertTable();
    }

    // создать таблицу, если ее нет
    public void createTable() {
        try {
            statement.executeUpdate("create table if not exists user (\n" +
                    "    id integer primary key autoincrement not null,\n" +
                    "    nick varchar unique not null,\n" +
                    "    login text unique not null,\n" +
                    "    pass TEXT NOT NULL\n" +
                    ");");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + throwables);
        }
    }

    // проверить, что таблица пустая
    public boolean tableIsEmpty() {
        boolean result = false;
        try (ResultSet resultSet = statement.executeQuery("SELECT * FROM user;")) {
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + e);
        }
        return !result;
    }


    // заполнить таблицу
    public void insertTable() {
        if (tableIsEmpty()) {
            try {
                statement.executeUpdate("INSERT INTO user (nick, login, pass) VALUES ('nick1', 'login1', 'pass1'), " +
                        "('nick2', 'login2', 'pass2'), " +
                        "('nick3', 'login3', 'pass3');");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                LOGGER.error("[ОШИБКА]: " + throwables);
            }
        }
    }

    @Override
    public void stop() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + e);
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + e);
        }
    }
    // получить ник пользователя
    @Override
    public String getNickByLoginPass(String login, String pass) {
        String nick = null;
        try {
            ResultSet resultSet = statement.executeQuery("SELECT nick FROM user WHERE login = '" + login + "' AND pass ='" + pass + "';");
            while(resultSet.next()){
                nick = resultSet.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LOGGER.info("[ОШИБКА]: " + throwables);
        }
        return nick;
    }

    // проверить, что ник свободен
    @Override
    public boolean isNickFree(String nick) {
        boolean result = false;
        try (ResultSet resultSet = statement.executeQuery("SELECT nick FROM user WHERE nick = '" + nick + "';")) {
            result = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.info("[ОШИБКА]: " + e);
        }
        return !result;
    }

    // получить id пользователя
    public int getIdUsers(String nick) {
        int id = 0;
        try {
            ResultSet resultSet = statement.executeQuery("SELECT id FROM user where nick = '" + nick + "';");
            while(resultSet.next()){
                id = resultSet.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LOGGER.info("[ОШИБКА]: " + throwables);
        }
        return id;
    }

    // обновить ник в таблице
    @Override
    public void updateNick(String newNick, String nick) {
        int id = getIdUsers(nick);
        try {
            statement.executeUpdate("UPDATE user SET nick = '" + newNick + "' WHERE (id = '" + id + "')");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LOGGER.info("[ОШИБКА]: " + throwables);
        }
    }

}
