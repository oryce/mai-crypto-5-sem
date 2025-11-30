package dora.messenger.server.event.rabbit;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        // TODO (17.12.25, ~oryce):
        //   Publisher confirms.
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("user.events", /* durable */ true, /* autoDelete */ false);
    }
}
