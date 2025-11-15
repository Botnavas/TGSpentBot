package dev.botnavas.tgspentbot;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j2
public class BotClass implements LongPollingSingleThreadUpdateConsumer {
    public BotClass() {

    }

    @Override
    public void consume(Update update) {

    }
}
