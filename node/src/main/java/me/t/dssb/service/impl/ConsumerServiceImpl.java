package me.t.dssb.service.impl;

import lombok.extern.log4j.Log4j;
import me.t.dssb.service.ConsumerService;
import me.t.dssb.service.MainService;
import me.t.dssb.service.ProducerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static me.t.dssb.model.RabbitQueue.*;

@Log4j
@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final MainService mainService;

    @Autowired
    public ConsumerServiceImpl(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    @RabbitListener(queues = TEXT_QUEUE)
    public void consumeTextMessage(Update update) {
        log.debug("NODE: Text Message Received");
        mainService.processTextMessage(update);
    }

    @Override
    @RabbitListener(queues = PHOTO_QUEUE)
    public void consumePhotoMessage(Update update) {
        log.debug("NODE: Photo Message Received");
    }

    @Override
    @RabbitListener(queues = DOC_QUEUE)
    public void consumeDocMessage(Update update) {
        log.debug("NODE: Doc Message Received");
    }
}
