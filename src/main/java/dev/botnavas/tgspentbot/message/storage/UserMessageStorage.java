package dev.botnavas.tgspentbot.message.storage;

import dev.botnavas.tgspentbot.message.model.UserMessage;

import java.util.Optional;

public interface UserMessageStorage {
    Optional<UserMessage> findById(long messageId, long userId);
    Optional<UserMessage> create(UserMessage message);
    Optional<UserMessage> update(UserMessage message, long oldID);
}
