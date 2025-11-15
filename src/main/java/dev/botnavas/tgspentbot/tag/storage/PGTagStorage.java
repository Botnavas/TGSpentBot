package dev.botnavas.tgspentbot.tag.storage;

import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.tag.model.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Log4j2
public class PGTagStorage implements TagStorage{
    private DBConnection connection;
    @Override
    public Optional<Tag> findById(int id) {
        try (var ps = connection.prepare(TagSql.FIND_BY_ID)) {
            ps.setLong(1, id);
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(Tag.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .userId(rs.getLong("user_id"))
                    .build());
        } catch (SQLException e) {
            log.error(String.format("Exception while finding tag by id %s: %s", id, e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public List<Tag> findUserTagsByUserId(long userId) {
        try (var ps = connection.prepare(TagSql.FIND_USER_TAGS_BY_USER_ID)) {
            ps.setLong(1, userId);
            var rs = ps.executeQuery();
            List<Tag> tags = new ArrayList<>();
            while (rs.next()) {
                Tag tag = Tag.builder()
                        .id(rs.getInt("id"))
                        .name(rs.getString("name"))
                        .userId(rs.getLong("user_id"))
                        .build();
                tags.add(tag);
            }

            return tags;

        } catch (SQLException e) {
            log.error(String.format("Exception while finding tags by user id %s: %s", userId, e.getMessage()));
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Tag> create(Tag tag) {
        try (var ps = connection.prepare(TagSql.CREATE)) {
            ps.setLong(1, tag.getUserId());
            ps.setString(2, tag.getName());
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            tag.setId(rs.getInt("id"));
            return Optional.of(tag);

        } catch (SQLException e) {
            log.error(String.format("Exception while creating tag:\n%s\nMessage: %s", tag.toString(), e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(Tag tag) {
        try (var ps = connection.prepare(TagSql.DELETE)) {
            ps.setLong(1, tag.getId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            log.error(String.format("Exception while deleting tag:\n%s\nMessage: %s", tag.toString(), e.getMessage()));
            return false;
        }
    }
}
