package dev.botnavas.tgspentbot.user.service;

import dev.botnavas.tgspentbot.user.model.User;

public interface UserService {
    boolean isRegistered(User user);
    void sendWelcomeMessage(User user);
}
