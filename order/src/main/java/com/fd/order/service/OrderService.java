package com.fd.order.service;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderEventProducer;
import com.fd.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventProducer producer;

    public OrderService(OrderRepository repository,
                        OrderEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public Order createOrder(Order order) {
    	
        order.setStatus(OrderStatus.CREATED);
        Order saved = repository.save(order);

        producer.publishOrderCreated(saved);

        return saved;
    }
    
    
    //this method retries for 3 times after 3 sec gap, otherwise sends to DLQ(dead letter queue)
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.setCommonErrorHandler(
                new DefaultErrorHandler(
                        new FixedBackOff(3000L, 3) // 3 retries, 3 sec gap
                )
        );

        return factory;
    }

    // this is a dead letter queue. whenever a msg keeps failing instead of blocking the customer we send the msg to DLQ.
    //acts as a detailed log for failed msgs keeping their msgs, topic, partitions, timestamps etc.
    //helpful for debugging
    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, ex) -> new TopicPartition(
                                record.topic() + "-dlt",
                                record.partition()
                        )
                );

        return new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(3000L, 3)
        );
    }

}
