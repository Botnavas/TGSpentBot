package org.main;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

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
            Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists users");
            statement.executeUpdate("create table users (id bigint, name string, status integer)");
        }
        catch(SQLException e)
        {
            e.printStackTrace(System.err);
        }
    }


    @Override
    public void CreateUser(String name, long id) {
        User user = new User(id, name);
        CreateUser(user);
    }

    @Override
    public void CreateUser(@NotNull User user) {
        if (CheckUser(user.getId())) {
            return;
        }
        String sql = "insert into users(id, name, status) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setInt(3, user.getStatus().getCode());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean CheckUser(long id) {
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
    public User GetUser(long id) {
        return null;
    }

    @Override
    public void ChangeUser(User user) {
        if (!CheckUser(user.getId())) {
            return;
        }

        String sql = "update users set name = ?, status = ? where id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(3, user.getId());
            pstmt.setString(1, user.getName());
            pstmt.setInt(2, user.getStatus().getCode());
            pstmt.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
