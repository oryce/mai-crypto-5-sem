package dora.messenger.server.event.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import dora.messenger.server.event.queue.Delivery;
import dora.messenger.server.event.queue.Envelope;
import dora.messenger.server.event.queue.EventQueue;
import dora.messenger.server.event.queue.Subscription;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Component
public class RabbitEventQueue implements EventQueue {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange directExchange;
    private final ObjectMapper objectMapper;
    private final ConnectionFactory connectionFactory;

    public RabbitEventQueue(
        RabbitAdmin rabbitAdmin,
        RabbitTemplate rabbitTemplate,
        DirectExchange directExchange,
        ObjectMapper objectMapper,
        ConnectionFactory connectionFactory
    ) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.directExchange = directExchange;
        this.objectMapper = objectMapper;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void publish(String queueName, Envelope event) throws IOException {
        ensureQueue(queueName);

        byte[] serializedEvent;
        try {
            serializedEvent = objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            throw new IOException("Cannot serialize event ", e);
        }

        rabbitTemplate.convertAndSend(directExchange.getName(), queueName, serializedEvent, (message) -> {
            MessageProperties properties = message.getMessageProperties();

            properties.setMessageId(event.id());
            properties.setContentType(MediaType.APPLICATION_JSON_VALUE);
            properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

            return message;
        });
    }

    @Override
    public Subscription subscribe(String queueName, Consumer<Delivery> consumer) throws IOException {
        String rabbitQueueName = ensureQueue(queueName);

        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(/* transactional */false);

        String consumerTag = channel.basicConsume(rabbitQueueName, /* autoAck */ false, new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(
                String consumerTag,
                com.rabbitmq.client.Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body
            ) throws IOException {
                var event = objectMapper.readValue(body, Envelope.class);
                var deliveryTag = envelope.getDeliveryTag();

                consumer.accept(new Delivery() {

                    @Override
                    public Envelope envelope() {
                        return event;
                    }

                    @Override
                    public void acknowledge() {
                        RabbitEventQueue.this.acknowledge(channel, deliveryTag);
                    }
                });
            }
        });

        return () -> unsubscribe(connection, channel, consumerTag);
    }

    private String ensureQueue(String queueName) {
        String rabbitQueueName = "%s.%s".formatted(directExchange.getName(), queueName);

        // TODO (17.12.25, ~oryce):
        //   Add TTL.
        Queue queue = QueueBuilder.durable(rabbitQueueName).build();
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(directExchange).with(queueName));

        return rabbitQueueName;
    }

    // FIXME (17.12.25, ~oryce):
    //   Hazard! Not thread-safe.

    private void unsubscribe(Connection connection, Channel channel, String consumerTag) {
        if (channel.isOpen()) {
            try {
                // FIXME (17.12.25, ~oryce):
                //   Channels are not actually closed because Spring caches them. If a client
                //   doesn't acknowledge an event and disconnects, it would not be re-sent to them.

                channel.basicCancel(consumerTag);
                channel.close();
            } catch (Exception ignored) {
            }
        }

        if (connection.isOpen()) {
            connection.close();
        }
    }

    private void acknowledge(Channel channel, long deliveryTag) {
        if (channel.isOpen()) {
            try {
                channel.basicAck(deliveryTag, false);
            } catch (IOException ignored) {
            }
        }
    }
}
