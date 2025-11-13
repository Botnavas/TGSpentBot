package dev.botnavas.tgspentbot.storage;

import dev.botnavas.tgspentbot.models.Expense;
import dev.botnavas.tgspentbot.models.Tag;
import dev.botnavas.tgspentbot.models.User;

import java.util.List;
import java.util.Optional;

public interface DBInterface {
    void createUser(String name, long id);
    void createUser(User user);
    boolean checkUser(long id);
    Optional<User> getUser(long id);
    void changeUser(User user);
    void setLastCommand(long id, String command);
    void clearLastCommand(long id);
    boolean checkTag(long uid, String tag);
    boolean checkTag(long uid, int tagId);
    boolean addTag(long uid, String tag);
    boolean deleteTag(long uid, String tag);
    List<Tag> getTagsModels(long uid);
    Tag getTagById(int id);
    int getTagId(String name, long uid);
    int addExpense(Expense expense);
    boolean checkExpense(int id);
    Expense getExpense(int id);
    boolean deleteExpense(int id);
    boolean updateExpense(Expense expense);
    List<String> getTags(long uid);
}
