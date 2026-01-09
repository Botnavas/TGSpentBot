package dev.botnavas.tgspentbot.userstate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class UserState {
    long userId;
    UserStates state;
    long botMessageId;
    UserRole role;

    public UserState setDeafult()
    {
        state = UserStates.DEFAULT;
        botMessageId = 0;
        return this;
    }
}
