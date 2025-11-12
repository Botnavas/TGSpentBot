package dev.botnavas.tgspentbot;

import models.Expense;
import models.Tag;
import models.User;

import java.util.List;

public interface DBInterface {
    void createUser(String name, long id);
    void createUser(User user);
    boolean checkUser(long id);
    User getUser(long id);
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
