package dev.botnavas.tgspentbot.user.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@FieldDefaults(level= AccessLevel.PRIVATE)
public class User {
    long id;
    long chatId;
    String userName;
    String firstName;
    String secondName;
    LocalDateTime lastInteraction;
}
