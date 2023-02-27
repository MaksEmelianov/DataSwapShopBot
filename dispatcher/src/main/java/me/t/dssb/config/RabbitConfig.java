package me.t.dssb.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

import static me.t.dssb.model.RabbitQueue.*;

@Configuration
@EnableRabbit
public class RabbitConfig {

    private final Environment environment;

    @Autowired
    public RabbitConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(Objects.requireNonNull(environment.getProperty("spring.rabbitmq.host")));
        factory.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.rabbitmq.port"))));
        factory.setUsername(Objects.requireNonNull(environment.getProperty("spring.rabbitmq.username")));
        factory.setPassword(Objects.requireNonNull(environment.getProperty("spring.rabbitmq.password")));
        return factory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_QUEUE);
    }

    @Bean
    public Queue docMessageQueue() {
        return new Queue(DOC_QUEUE);
    }

    @Bean
    public Queue photoMessageQueue() {
        return new Queue(PHOTO_QUEUE);
    }

    @Bean
    public Queue answerMessageQueue() {
        return new Queue(ANSWERS_QUEUE);
    }
}
