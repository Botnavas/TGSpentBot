package dev.botnavas.tgspentbot.storage.impl;
import dev.botnavas.tgspentbot.models.Expense;
import dev.botnavas.tgspentbot.models.Tag;
import dev.botnavas.tgspentbot.user.model.User;
import dev.botnavas.tgspentbot.storage.DBInterface;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class SqlLiteImpl implements DBInterface {
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
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public SqlLiteImpl(String url)
    {
        try {
            connection = DriverManager.getConnection(url);

            Statement pragmaStatement = connection.createStatement();
            pragmaStatement.execute("PRAGMA foreign_keys = ON");
            pragmaStatement.close();

            Statement statement = connection.createStatement();
            statement.executeUpdate("create table if not exists users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name string,  " +
                    "last_command string,  " +
                    "status integer)");

            statement.executeUpdate("create table if not exists tags (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id bigint NOT NULL, " +
                    "name string NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "UNIQUE(user_id, name))");

            statement.executeUpdate("create table if not exists expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id bigint not null, " +
                    "tag_id INTEGER NOT NULL, " +
                    "amount INTEGER NOT NULL CHECK (amount >= 0), " +
                    "date DATE not null, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE)");

            statement.close();
        }
        catch(SQLException e) {
            e.printStackTrace(System.err);
        }
    }


    @Override
    public void createUser(String name, long id) {
        createUser(new User(id, name));
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
            log.error(String.format("Exception while creating user: %s", e.getMessage()));
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
            log.error("");
        }
        return false;
    }

    @Override
    public Optional<User> getUser(long id) {
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
                return Optional.of(user);
            }

            return Optional.empty();
        }
        catch (SQLException e) {
            log.error(String.format("Error while getting user with id %s: %s", id, e.getMessage()));
            return Optional.empty();
        }
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
        var user = getUser(id);
        if (user.isEmpty()) {
            return;
        }
        user.get().setLastCommand(command);
        changeUser(user.get());
    }

    @Override
    public void clearLastCommand(long id) {
        setLastCommand(id, "");
    }

    @Override
    public boolean checkTag(long uid, String tag) {
        String sql = "select id from tags where user_id = ? AND name = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, uid);
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
    public boolean checkTag(long uid, int tagId) {
        String sql = "select id from tags where user_id = ? AND id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, uid);
            pstmt.setInt(2, tagId);
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
    public boolean addTag(long uid, String tag) {
        if (checkTag(uid, tag)) {
            return false;
        }
        String sql = "insert into tags (user_id, name) values (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, uid);
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
    public boolean deleteTag(long uid, String tag) {
        if (!checkTag(uid, tag)) {
            return false;
        }
        String sql = "delete from tags where user_id = ? and name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, uid);
            pstmt.setString(2, tag);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    @Override
    public List<Tag> getTagsModels(long uid) {
        List<Tag> tags = new ArrayList<>();
        String sql = "select name, id from tags where user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, uid);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
             Tag tag = new Tag(rs.getInt("id"), uid, rs.getString("name"));
             tags.add(tag);
            }

            return tags;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Tag getTagById(int id) {
        String sql = "select name, user_id from tags where id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Tag(id, rs.getLong("user_id"), rs.getString("name"));
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getTagId(String name, long uid) {
        String sql = "select id from tags where name = ? and user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(2, uid);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return  rs.getInt("id");
            }

            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int addExpense(Expense expense) {
        String sql = "insert into expenses (user_id, tag_id, amount, date) values (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, expense.getUid());
            pstmt.setInt(2, expense.getTagId());
            pstmt.setInt(3, expense.getSum());
            pstmt.setDate(4, Date.valueOf(expense.getDate()));
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

            } catch (SQLException e) {
                e.printStackTrace(System.err);
                return -1;
            }

        } catch (SQLException e) {
                e.printStackTrace(System.err);
                return -1;
        }
        return -1;
    }

    @Override
    public boolean checkExpense(int id) {
        String sql = "select * from expenses where id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
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
    public Expense getExpense(int id) {
        String sql = "select * from expenses where id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Expense(
                        rs.getInt("id"),
                        rs.getLong("user_id"),
                        rs.getInt("tag_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getInt("amount")
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET user_id = ?, tag_id = ?, amount = ?, date = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // Установка новых значений:
            pstmt.setLong(1, expense.getUid());
            pstmt.setInt(2, expense.getTagId());
            pstmt.setInt(3, expense.getSum());
            pstmt.setDate(4, Date.valueOf(expense.getDate()));
            pstmt.setInt(5, expense.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getTags(long uid) {
        List<String> tags = new ArrayList<>();

        String sql = "select name from tags where user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, uid);
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
