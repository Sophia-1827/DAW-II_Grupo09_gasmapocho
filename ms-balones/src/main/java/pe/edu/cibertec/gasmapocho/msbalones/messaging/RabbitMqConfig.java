package pe.edu.cibertec.gasmapocho.msbalones.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "gas-mapocho.movimientos";
    public static final String ROUTING_KEY = "movimiento.registrado";
    public static final String QUEUE_STOCK = "balones.stock.queue";

    @Bean
    public TopicExchange movimientosExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue stockQueue() {
        return new Queue(QUEUE_STOCK, true);
    }

    @Bean
    public Binding stockBinding() {
        return BindingBuilder.bind(stockQueue()).to(movimientosExchange()).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
