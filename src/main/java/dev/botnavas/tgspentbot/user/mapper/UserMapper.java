package dev.botnavas.tgspentbot.user.mapper;

import dev.botnavas.tgspentbot.user.model.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public class UserMapper {
    public static User fromTelegram(Message msg) {
        var user = msg.getFrom();
        return User.builder()
                .id(user.getId())
                .chatId(msg.getChatId())
                .userName(user.getUserName())
                .firstName(user.getFirstName())
                .secondName(user.getLastName())
                .build();
    }
}
