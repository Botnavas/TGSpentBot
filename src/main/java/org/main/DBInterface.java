package org.main;

public interface DBInterface {
    void CreateUser(String name, long id);
    void CreateUser(User user);
    boolean CheckUser(long id);
    User GetUser(long id);

    void ChangeUser(User user);
}
