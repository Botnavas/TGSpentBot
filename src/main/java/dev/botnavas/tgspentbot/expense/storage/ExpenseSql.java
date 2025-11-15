package dev.botnavas.tgspentbot.expense.storage;

public class ExpenseSql {
    public static final String FIND_BY_ID = "SELECT * FROM exchange.expenses " +
            "WHERE id = ?";
    public static final String CREATE = "INSERT INTO exchange.expenses" +
            "(user_id, tag_id, sum, date) " +
            "VALUES(?, ?, ?, ?)" +
            "RETURNING id";
    public static final String DELETE = "DELETE FROM exchange.expenses " +
            "WHERE id = ?";
}
