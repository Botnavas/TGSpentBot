package dev.botnavas.tgspentbot.user.mapper;

import dev.botnavas.tgspentbot.user.model.User;
import dev.botnavas.tgspentbot.user.service.UserService;
import dev.botnavas.tgspentbot.user.storage.UserStorage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Optional;

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

    public static Optional<User> fromCallbackQuery(CallbackQuery query, UserService service) {
        var message = query.getMessage();
            long chatId = message.getChatId();
            return service.findByChatId(chatId);

    }
}
