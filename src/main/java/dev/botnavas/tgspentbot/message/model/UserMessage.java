package dev.botnavas.tgspentbot.message.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class UserMessage {
    Long messageId;
    MessageState state;
    LocalDateTime sent;
    int sum;
    LocalDate date;
    int tagId;
}
