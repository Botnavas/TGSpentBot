package dev.botnavas.tgspentbot.user.service;

import dev.botnavas.tgspentbot.user.model.User;
import dev.botnavas.tgspentbot.userstate.model.UserState;

import java.util.Optional;

public interface UserService {
    boolean isRegistered(User user);
    void sendWelcomeMessage(User user);
    void sendMainMenu(User user);
    Optional<User> findByChatId(long chatId);
    void handleNewTagCommand(User user);
    UserState getState(User user);
    void addNewTag(String tag, User user);
    void handleDeleteTagCommand(User user, int tagId, int messageId);
    void handleTagDeleteMenuCommand(User user, int messageId);
}
