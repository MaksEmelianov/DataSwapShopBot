package me.t.dssb.controller;

import lombok.extern.log4j.Log4j;
import me.t.dssb.model.RabbitQueue;
import me.t.dssb.service.UpdateProducer;
import me.t.dssb.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@Component
public class UpdateController {
    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;

    @Autowired
    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    /**
     * Регистрация телеграм бота, внедрить бин нельзя из-за циклической зависимости
     * Можно заменить на сеттер и в самом классе убрать init()
     * @param telegramBot
     */
    public void register(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    /**
     * Проверка типа сообщения
     * @param update
     */
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

    /**
     * Распределение сообщений по типу для брокера сообщений
     * Документ, фото, текст
     * @param update
     */
    private void distributeMessagesByType(Update update) {
        Message message = update.getMessage();
        if (message.getText() != null) {
            processTextMessage(update);
        } else if (message.getPhoto()!= null) {
            processPhotoMessage(update);
        } else if (message.getDocument()!= null) {
            processDocumentMessage(update);
        } else {
            setUnsupportedMessageTypeView(update);
        }
    }

    private void setView(SendMessage message) {
        telegramBot.sendMessage(message);
    }

    private void setUnsupportedMessageTypeView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(
                update,
                "Unsupported Message Type"
        );
        setView(sendMessage);
    }

    private void setFileReceivedView(Update update) {
        SendMessage sendMessage = messageUtils.generateSendMessageWithText(
                update,
                "File processed"
        );
        setView(sendMessage);
    }

    private void processDocumentMessage(Update update) {
        updateProducer.produce(RabbitQueue.DOC_QUEUE, update);
        setFileReceivedView(update);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(RabbitQueue.PHOTO_QUEUE, update);
        setFileReceivedView(update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(RabbitQueue.TEXT_QUEUE, update);
        setFileReceivedView(update);
    }
}
