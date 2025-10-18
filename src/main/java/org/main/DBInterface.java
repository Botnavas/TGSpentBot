package org.main;

import java.util.List;

public interface DBInterface {
    void createUser(String name, long id);
    void createUser(User user);
    boolean checkUser(long id);
    User getUser(long id);
    void changeUser(User user);
    void setLastCommand(long id, String command);
    boolean checkTag(long id, String tag);
    boolean addTag(long id, String tag);
    boolean deleteTag(long id, String tag);
    List<String> getTags(long id);
}
