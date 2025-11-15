package dev.botnavas.tgspentbot.expense.storage;

import dev.botnavas.tgspentbot.expense.model.Expense;
import dev.botnavas.tgspentbot.storage.model.DBConnection;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Optional;

@AllArgsConstructor
@Log4j2
public class PGExpenseStorage implements ExpenseStorage{
    private DBConnection connection;
    @Override
    public Optional<Expense> findById(int id) {
        try (var ps = connection.prepare(ExpenseSql.FIND_BY_ID)) {
            ps.setLong(1, id);
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(Expense.builder()
                    .id(rs.getInt("id"))
                    .userId(rs.getLong("user_id"))
                    .tagId(rs.getInt("tag_id"))
                    .sum(rs.getInt("sum"))
                    .date(rs.getObject("date", LocalDate.class))
                    .build());
        } catch (SQLException e) {
            log.error(String.format("Exception while finding expense by id %s: %s", id, e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<Expense> create(Expense expense) {
        try (var ps = connection.prepare(ExpenseSql.CREATE)) {
            ps.setLong(1, expense.getUserId());
            ps.setInt(2, expense.getTagId());
            ps.setInt(3, expense.getSum());
            ps.setObject(4, expense.getDate(), Types.DATE);
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            expense.setId(rs.getInt("id"));
            return Optional.of(expense);

        } catch (SQLException e) {
            log.error(String.format("Exception while creating expense:\n%s\nMessage: %s", expense.toString(), e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(Expense expense) {
        try (var ps = connection.prepare(ExpenseSql.DELETE)) {
            ps.setLong(1, expense.getId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error(String.format("Exception while deleting expense:\n%s\nMessage: %s", expense.toString(), e.getMessage()));
            return false;
        }
    }
}
