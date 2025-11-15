package dev.botnavas.tgspentbot.tag.storage;

import dev.botnavas.tgspentbot.tag.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagStorage {
    Optional<Tag> findById(int id);
    List<Tag> findUserTagsByUserId(long userId);
    Optional<Tag> create(Tag tag);
    boolean delete(Tag tag);
}
