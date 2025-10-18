package org.main;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBClass implements DBInterface{
   /* public static void main(String[] args) throws SQLException {

        System.out.println("Hello world!");
        try {
            String url = AppConfig.getDatabaseUrl();
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            e.printStackTrace(System.err);
        }
    }*/

    Connection connection;

    DBClass(String url)
    {
        try {
            connection = DriverManager.getConnection(url);
            /* Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists users");
            statement.executeUpdate("create table users (id bigint primary key, name string, status integer)");*/

            Statement pragmaStatement = connection.createStatement();
            pragmaStatement.execute("PRAGMA foreign_keys = ON");
            pragmaStatement.close();

            // Создаем таблицы
            /*Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists expenses");
            statement.executeUpdate("create table if not exists tags");
            statement.executeUpdate("drop table if exists users");*/

            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name string,  " +
                    "last_command string,  " +
                    "status integer)");

            statement.executeUpdate("create table if not exists tags (" +
                    "id integer primary key autoincrement, " +
                    "user_id bigint not null, " +
                    "name string not null, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "UNIQUE(user_id, name))");

            statement.executeUpdate("create table if not exists expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id bigint not null, " +
                    "tag_id INTEGER NOT NULL, " +
                    "amount REAL NOT NULL CHECK (amount >= 0), " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE)");

            statement.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }


    @Override
    public void createUser(String name, long id) {
        User user = new User(id, name);
        createUser(user);
    }

    @Override
    public void createUser(@NotNull User user) {
        if (checkUser(user.getId())) {
            return;
        }
        String sql = "insert into users(id, name, status, last_command) VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setInt(3, user.getStatus().getCode());
            pstmt.setString(4, user.getLastCommand());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkUser(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public User getUser(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setName(rs.getString("name"));
                user.setStatus(User.UserStatus.fromCode(rs.getInt("status")));
                user.setLastCommand(rs.getString("last_command"));
                return user;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    public void changeUser(User user) {
        if (!checkUser(user.getId())) {
            return;
        }

        String sql = "update users set name = ?, status = ?, last_command = ? where id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(4, user.getId());
            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getStatus().getCode());
            pstmt.setString(3, user.getLastCommand());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLastCommand(long id, String command) {
        User user = getUser(id);
        user.setLastCommand(command);
        changeUser(user);
    }

    @Override
    public boolean checkTag(long id, String tag) {
        String sql = "select id from tags where user_id = ? AND name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.setString(2, tag);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addTag(long id, String tag) {
        if (checkTag(id, tag)) {
            return false;
        }
        String sql = "insert into tags (user_id, name) values (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.setString(2, tag);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) { // Код ошибки нарушения UNIQUE constraint
                System.out.println("Тег '" + tag + "' уже существует для этого пользователя");
            } else {
                e.printStackTrace(System.err);
            }
            return false;
        }
    }

    @Override
    public boolean deleteTag(long id, String tag) {
        if (!checkTag(id, tag)) {
            return false;
        }
        String sql = "delete from tags where user_id = ? and name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.setString(2, tag);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    @Override
    public List<String> getTags(long id) {
        List<String> tags = new ArrayList<>();

        String sql = "select name from tags where user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tags.add(rs.getString("name"));
            }

            return tags;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
