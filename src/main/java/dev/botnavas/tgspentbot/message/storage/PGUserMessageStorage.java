package dev.botnavas.tgspentbot.message.storage;

import dev.botnavas.tgspentbot.message.model.MessageState;
import dev.botnavas.tgspentbot.message.model.UserMessage;
import dev.botnavas.tgspentbot.storage.model.DBConnection;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@Log4j2
public class PGUserMessageStorage implements UserMessageStorage {
    private DBConnection connection;

    @Override
    public Optional<UserMessage> findById(long messageId, long userId) {
        try (var ps = connection.prepare(UserMessagesSql.FIND_BY_ID)) {
            ps.setLong(1, messageId);
            ps.setLong(2, userId);
            var rs = ps.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(UserMessage.builder()
                    .messageId(rs.getLong("message_id"))
                    .state(MessageState.fromString(rs.getString("state")))
                    .sent(rs.getObject("sent", LocalDateTime.class))
                    .sum(rs.getInt("sum"))
                    .date(rs.getObject("date", LocalDate.class))
                    .tagId(rs.getInt("tag_id"))
                    .userId(rs.getLong("user_id"))
                    .expenseId(rs.getInt("expense_id"))
                    .build());
        } catch (SQLException e) {
            log.error(String.format("Exception while finding user message by message id %s: %s", messageId, e.getMessage()));
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.error(String.format("Exception while parsing message state for id %s: %s", messageId, e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserMessage> create(UserMessage message) {
        try (var ps = connection.prepare(UserMessagesSql.CREATE)) {
            ps.setLong(1, message.getMessageId());
            ps.setString(2, message.getState().toString());
            ps.setObject(3, message.getSent(), Types.TIMESTAMP);
            ps.setInt(4, message.getSum());
            ps.setObject(5, message.getDate(), Types.DATE);
            ps.setInt(6, message.getTagId());
            ps.setLong(7, message.getUserId());
            ps.setInt(8, message.getExpenseId());

            ps.executeUpdate();

            return Optional.of(message);
        } catch (SQLException e) {
            log.error(String.format("Exception while creating user message:\n%s\nMessage: %s", message.toString(), e.getMessage()));
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserMessage> update(UserMessage message, long oldID) {
        try (var ps = connection.prepare(UserMessagesSql.UPDATE)) {
            ps.setLong(1, message.getMessageId());
            ps.setString(2, message.getState().toString());
            ps.setObject(3, message.getSent(), Types.TIMESTAMP);
            ps.setInt(4, message.getSum());
            ps.setObject(5, message.getDate(), Types.DATE);
            ps.setInt(6, message.getTagId());
            ps.setInt(7, message.getExpenseId());

            ps.setLong(8, oldID);
            ps.setLong(9, message.getUserId());

            ps.executeUpdate();

            return Optional.of(message);
        } catch (SQLException e) {
            log.error(String.format("Exception while updating user message:\n%s\nMessage: %s", message.toString(), e.getMessage()));
            return Optional.empty();
        }
    }
}
