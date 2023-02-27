package me.t.dssb.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@Component
public class UpdateController {
    private TelegramBot telegramBot;

    public void register(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }
        if (update.getMessage() != null) {
            distributeMessagesByType(update);
        } else {
            log.error("Received unsupported message type " + update.getMessage());
        }
    }

    private void distributeMessagesByType(Update update) {
        
    }
}
