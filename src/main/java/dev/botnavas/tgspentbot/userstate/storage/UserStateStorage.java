package dev.botnavas.tgspentbot.userstate.storage;

import dev.botnavas.tgspentbot.userstate.model.UserState;

import java.util.Optional;

public interface UserStateStorage {
    Optional<UserState> findByUserId(long userId);
    Optional<UserState> create(UserState userState);
    Optional<UserState> update(UserState userState);
}
