package com.fd.order.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import com.fd.events.DeliveryEvent;
import com.fd.events.PaymentEvent;

@Configuration
@EnableKafka
public class KafkaConfig {

    //COMMON ERROR HANDLER 

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        FixedBackOff backOff = new FixedBackOff(3000L, 3);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    // PAYMENT EVENT CONSUMER

    @Bean
    public ConsumerFactory<String, PaymentEvent> paymentConsumerFactory() {

        JsonDeserializer<PaymentEvent> deserializer =
                new JsonDeserializer<>(PaymentEvent.class);

        deserializer.addTrustedPackages(
                "com.fd.order.model",
                "com.example.delivery.model"
        );
        deserializer.setUseTypeHeaders(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-payment-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent>
    paymentKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentEvent> paymentConsumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentConsumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }

    // DELIVERY EVENT CONSUMER 

    @Bean
    public ConsumerFactory<String, DeliveryEvent> deliveryConsumerFactory() {

        JsonDeserializer<DeliveryEvent> deserializer =
                new JsonDeserializer<>(DeliveryEvent.class);

        deserializer.addTrustedPackages(
                "com.fd.order.model",
                "com.example.delivery.model"
        );
        deserializer.setUseTypeHeaders(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-delivery-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryEvent>
    deliveryKafkaListenerContainerFactory(
            ConsumerFactory<String, DeliveryEvent> deliveryConsumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, DeliveryEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(deliveryConsumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}
