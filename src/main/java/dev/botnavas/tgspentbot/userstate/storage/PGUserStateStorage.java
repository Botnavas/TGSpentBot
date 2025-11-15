package dev.botnavas.tgspentbot.userstate.storage;

import dev.botnavas.tgspentbot.storage.model.DBConnection;
import dev.botnavas.tgspentbot.userstate.model.UserRole;
import dev.botnavas.tgspentbot.userstate.model.UserState;
import dev.botnavas.tgspentbot.userstate.model.UserStates;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
@Log4j2
public class PGUserStateStorage implements UserStateStorage {
    private DBConnection connection;

    @Override
    public Optional<UserState> findByUserId(long userId) {
        try (var ps = connection.prepare(UserStateSql.FIND_BY_ID)) {
            ps.setLong(1, userId);
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(UserState.builder()
                    .userId(rs.getLong("user_id"))
                    .state(UserStates.valueOf(rs.getString("state")))
                    .botMessageId(rs.getLong("bot_message_id"))
                    .role(UserRole.valueOf(rs.getString("role")))
                    .build());
        } catch (SQLException e) {
            log.error(String.format("Exception while finding user state by user id %s: %s", userId, e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserState> create(UserState userState) {
        try (var ps = connection.prepare(UserStateSql.CREATE)) {
            ps.setLong(1, userState.getUserId());
            ps.setString(2, userState.getState().toString());
            ps.setLong(3, userState.getBotMessageId());
            ps.setString(4, userState.getRole().toString());

            ps.executeUpdate();

            return  Optional.of(userState);
        } catch(SQLException e) {
            log.error(String.format("Exception while creating user state:\n%s\nMessage: %s", userState.toString(), e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserState> update(UserState userState) {
        try (var ps = connection.prepare(UserStateSql.UPDATE)) {
            ps.setString(1, userState.getState().toString());
            ps.setLong(2, userState.getBotMessageId());
            ps.setString(3, userState.getRole().toString());
            ps.setLong(4, userState.getUserId());

            ps.executeUpdate();

            return  Optional.of(userState);
        } catch (SQLException e) {
            log.error(String.format("Exception while updating user state:\n%s\nMessage: %s", userState.toString(), e.getMessage()));
            return Optional.empty();
        }
    }


}
