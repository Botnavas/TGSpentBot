package dev.botnavas.tgspentbot.user.storage;

import dev.botnavas.tgspentbot.user.model.User;

import java.util.Optional;

public interface UserStorage {
    Optional<User> findById(long userId);
    Optional<User> createUser(User user);
}
