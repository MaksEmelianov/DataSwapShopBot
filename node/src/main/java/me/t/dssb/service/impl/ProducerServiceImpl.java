package me.t.dssb.service.impl;

import me.t.dssb.service.ProducerService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static me.t.dssb.model.RabbitQueue.ANSWERS_QUEUE;

@Service
public class ProducerServiceImpl implements ProducerService {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public ProducerServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWERS_QUEUE, sendMessage);
    }
}
