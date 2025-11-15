package dev.botnavas.tgspentbot.user.storage;

import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.user.model.User;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Data
public class PGUserStorage implements UserStorage {
    private final DBConnection connection;

    @Override
    public Optional<User> findById(long userId) {
        try (var ps = connection.prepare(UserSql.FIND_BY_ID)) {
            ps.setLong(1, userId);
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(User.builder()
                    .id(rs.getLong("id"))
                    .chatId(rs.getLong("chat_id"))
                    .userName(rs.getString("username"))
                    .firstName(rs.getString("first_name"))
                    .secondName(rs.getString("second_name"))
                    .lastInteraction(rs.getObject("last_interaction_dttm", LocalDateTime.class))
                    .build());
        } catch (SQLException e) {
            log.error(String.format("Exception while finding user with %s: %s", userId, e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> createUser(User user) {
        try (var ps = connection.prepare(UserSql.CREATE_USER)) {
            ps.setLong(1, user.getId());
            ps.setLong(2, user.getChatId());
            ps.setString(3, user.getUserName());
            ps.setString(4, user.getFirstName());
            ps.setString(5, user.getSecondName());
            ps.setObject(6, LocalDateTime.now(), Types.TIMESTAMP);

            ps.executeUpdate();

            return Optional.of(user);
        } catch (SQLException e) {
            log.error(String.format("Exception while creating user:\n%s\nMessage: %s", user.toString(), e.getMessage()));
            return Optional.empty();
        }
    }
}
