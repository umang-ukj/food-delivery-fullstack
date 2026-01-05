package com.fd.order.event.consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.OrderConfirmedEvent;
import com.fd.events.OrderEvent;
import com.fd.events.PaymentEvent;
import com.fd.events.PaymentStatus;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderConfirmedEventProducer;
import com.fd.order.repository.OrderRepository;

@Component
public class PaymentEventConsumer {

	private static final Logger log =
			LoggerFactory.getLogger(PaymentEventConsumer.class);
	
	private final OrderConfirmedEventProducer orderConfirmedEventProducer;
	private final OrderRepository repository;
	private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

	public PaymentEventConsumer(
	        OrderRepository repository,
	        KafkaTemplate<String, OrderEvent> kafkaTemplate,OrderConfirmedEventProducer orderConfirmedEventProducer) {
	    this.repository = repository;
	    this.kafkaTemplate = kafkaTemplate;
	    this.orderConfirmedEventProducer=orderConfirmedEventProducer;
	}


	@KafkaListener(topics = "payment-events",containerFactory = "paymentKafkaListenerContainerFactory")
	public void handlePaymentEvent(PaymentEvent event) {

		log.info("Received PAYMENT_{} event for orderId={}",
				event.getStatus(), event.getOrderId());

		Order order = repository.findById(event.getOrderId())
				.orElseThrow();

		if (event.getStatus() == PaymentStatus.PAYMENT_SUCCESS) {

			/*
			 * if (order.getStatus() != OrderStatus.CREATED && order.getStatus() !=
			 * OrderStatus.PAYMENT_PENDING) { return; }
			 */
		    order.setStatus(OrderStatus.CONFIRMED);
		    OrderConfirmedEvent confirmedEvent =
		            new OrderConfirmedEvent(order.getId());

		    orderConfirmedEventProducer.publish(confirmedEvent);

		    log.info("Published ORDER_CONFIRMED event for orderId={}", order.getId());

		    repository.save(order);

			/*
			 * // publish paid event kafkaTemplate.send( "order-events", new
			 * OrderEvent(order.getId(), null, "PAID") );
			 */
		    log.info("Order {} CONFIRMED after successful payment", order.getId());
		    log.info("Order {} confirmed, triggering delivery flow", order.getId());

		}

		if (event.getStatus() == PaymentStatus.PAYMENT_FAILED) {

		    order.setStatus(OrderStatus.PAYMENT_FAILED);
		    repository.save(order);

			/*
			 * kafkaTemplate.send( "order-events", new OrderEvent(order.getId(), null,
			 * "FAILED") );
			 */
		    log.warn("Order {} marked as FAILED and event published", order.getId());
		}

		//repository.save(order);
	}
}
