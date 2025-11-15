package dev.botnavas.tgspentbot.expense.storage;

import dev.botnavas.tgspentbot.expense.model.Expense;

import java.util.Optional;

public interface ExpenseStorage {
    Optional<Expense> findById(int id);
    Optional<Expense> create(Expense expense);
    boolean delete(Expense expense);
}
