package me.t.dssb.service.impl;

import me.t.dssb.controller.UpdateController;
import me.t.dssb.service.AnswerConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static me.t.dssb.model.RabbitQueue.ANSWERS_QUEUE;

@Service
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateController updateController;

    @Autowired
    public AnswerConsumerImpl(UpdateController updateController) {
        this.updateController = updateController;
    }

    @Override
    @RabbitListener(queues = ANSWERS_QUEUE)
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);
    }
}
