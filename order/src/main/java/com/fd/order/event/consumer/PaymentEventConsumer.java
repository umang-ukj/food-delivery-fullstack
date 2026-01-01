package com.fd.order.event.consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.OrderEvent;
import com.fd.events.PaymentEvent;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.repository.OrderRepository;

@Component
public class PaymentEventConsumer {

	private static final Logger log =
			LoggerFactory.getLogger(PaymentEventConsumer.class);

	private final OrderRepository repository;
	private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

	public PaymentEventConsumer(
	        OrderRepository repository,
	        KafkaTemplate<String, OrderEvent> kafkaTemplate) {
	    this.repository = repository;
	    this.kafkaTemplate = kafkaTemplate;
	}


	@KafkaListener(topics = "payment-events",containerFactory = "paymentKafkaListenerContainerFactory")
	public void handlePaymentEvent(PaymentEvent event) {

		log.info("Received PAYMENT_{} event for orderId={}",
				event.getStatus(), event.getOrderId());

		Order order = repository.findById(event.getOrderId())
				.orElseThrow();

		if ("PAID".equals(event.getStatus())) {

		    if (order.getStatus() != OrderStatus.CREATED &&
		        order.getStatus() != OrderStatus.PAYMENT_PENDING) {
		        return;
		    }

		    order.setStatus(OrderStatus.PAID);
		    repository.save(order);

			/*
			 * // publish paid event kafkaTemplate.send( "order-events", new
			 * OrderEvent(order.getId(), null, "PAID") );
			 */
		    log.info("Order {} marked as PAID and event published", order.getId());
		}

		if ("FAILED".equals(event.getStatus())) {

		    order.setStatus(OrderStatus.FAILED);
		    repository.save(order);

			/*
			 * kafkaTemplate.send( "order-events", new OrderEvent(order.getId(), null,
			 * "FAILED") );
			 */
		    log.warn("Order {} marked as FAILED and event published", order.getId());
		}

		repository.save(order);
	}
}
