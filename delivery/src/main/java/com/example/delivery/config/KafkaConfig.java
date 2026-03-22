package com.example.delivery.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
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

import com.fd.events.OrderCancelledEvent;
import com.fd.events.OrderConfirmedEvent;

@Configuration
@EnableKafka
public class KafkaConfig {
	@Autowired
    private KafkaProperties kafkaProperties;

    public Map<String, Object> consumerConfigs() {
        return kafkaProperties.buildConsumerProperties();
    }
    
	@Bean
	public ConsumerFactory<String, OrderConfirmedEvent> orderConfirmedConsumerFactory() {

	    JsonDeserializer<OrderConfirmedEvent> deserializer =new JsonDeserializer<>(OrderConfirmedEvent.class);

	    deserializer.addTrustedPackages("*");
	    deserializer.setUseTypeHeaders(false);

	    return new DefaultKafkaConsumerFactory<>(
	            consumerConfigs(),
	            new StringDeserializer(),
	            deserializer
	    );
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent>
	orderConfirmedKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {

	    ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> factory =new ConcurrentKafkaListenerContainerFactory<>();

	    factory.setConsumerFactory(orderConfirmedConsumerFactory());
	    factory.setCommonErrorHandler(kafkaErrorHandler);
	    factory.getContainerProperties().setObservationEnabled(true);
	    return factory;
	}
	
	@Bean
    public ConsumerFactory<String, OrderCancelledEvent> orderCancelledConsumerFactory() {
        JsonDeserializer<OrderCancelledEvent> deserializer =new JsonDeserializer<>(OrderCancelledEvent.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                deserializer
        );
    }
	
	@Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent>
    orderCancelledKafkaListenerContainerFactory(DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCancelledEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(orderCancelledConsumerFactory());
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }
	
	@Bean
    public DefaultErrorHandler kafkaErrorHandler(@Qualifier("deadLetterKafkaTemplate") KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler =new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}
