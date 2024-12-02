package edu.stanford.protege.versioning;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SpringBeanConfig {

    @Bean
    @Lazy
    RpcRequestProcessor rpcRequestProcessor(ObjectMapper objectMapper,
                                            Messenger messenger) {
        return new RpcRequestProcessor(messenger, objectMapper);
    }

    @Bean
    @Lazy
    Messenger messageHandler(AsyncRabbitTemplate rabbitTemplate) {
        return new MessengerImpl(rabbitTemplate);
    }
}
