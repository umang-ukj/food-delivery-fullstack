package com.example.delivery.config;

import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

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

	    JsonDeserializer<OrderConfirmedEvent> deserializer =
	            new JsonDeserializer<>(OrderConfirmedEvent.class);

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
	orderConfirmedKafkaListenerContainerFactory() {

	    ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();

	    factory.setConsumerFactory(orderConfirmedConsumerFactory());
	    return factory;
	}

}
