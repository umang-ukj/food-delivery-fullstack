package com.fd.order.event.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.OrderConfirmedEvent;
import com.fd.events.OrderEvent;
import com.fd.events.PaymentEvent;
import com.fd.events.PaymentMethod;
import com.fd.events.PaymentStatus;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderConfirmedEventProducer;
import com.fd.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Component
public class PaymentEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

	private final OrderConfirmedEventProducer orderConfirmedEventProducer;
	private final OrderRepository repository;
	private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

	public PaymentEventConsumer(OrderRepository repository, KafkaTemplate<String, OrderEvent> kafkaTemplate,
			OrderConfirmedEventProducer orderConfirmedEventProducer) {
		this.repository = repository;
		this.kafkaTemplate = kafkaTemplate;
		this.orderConfirmedEventProducer = orderConfirmedEventProducer;
	}

	/*
	 * @KafkaListener(topics = "payment-events",containerFactory =
	 * "paymentKafkaListenerContainerFactory")
	 * 
	 * public void handlePaymentEvent(PaymentEvent event) {
	 * 
	 * Order order = repository.findById(event.getOrderId()) .orElseThrow();
	 * 
	 * // Ignore CASH payment events if (event.getMethod() == PaymentMethod.CASH) {
	 * 
	 * if (order.getStatus() == OrderStatus.CONFIRMED) { return; }
	 * 
	 * order.setStatus(OrderStatus.CONFIRMED); repository.save(order);
	 * 
	 * orderConfirmedEventProducer.publish( new OrderConfirmedEvent(order.getId())
	 * );
	 * 
	 * log.info("Order {} confirmed with CASH payment", order.getId()); return; }
	 * 
	 * 
	 * 
	 * // Only act on successful ONLINE payments if (event.getStatus() ==
	 * PaymentStatus.PAYMENT_SUCCESS) {
	 * 
	 * // idempotency guard if (order.getStatus() == OrderStatus.CONFIRMED) {
	 * return; }
	 * 
	 * order.setStatus(OrderStatus.CONFIRMED); repository.save(order);
	 * 
	 * // THIS WAS MISSING orderConfirmedEventProducer.publish( new
	 * OrderConfirmedEvent(order.getId()) );
	 * 
	 * log.info("Order {} confirmed after online payment", order.getId()); } }
	 */
	@Transactional
	@KafkaListener(topics = "payment-events", containerFactory = "paymentKafkaListenerContainerFactory")
	public void handlePaymentEvent(PaymentEvent event) {

		// Ignore non-success ONLINE payments
		if (event.getMethod() != PaymentMethod.CASH && event.getStatus() != PaymentStatus.PAYMENT_SUCCESS) {
			return;
		}

		int updated = repository.confirmIfNotConfirmed(event.getOrderId());

		if (updated == 0) {
			log.info("Order {} already confirmed. Skipping publish.", event.getOrderId());
			return;
		}

		orderConfirmedEventProducer.publish(new OrderConfirmedEvent(event.getOrderId()));

		log.info("Order {} confirmed and OrderConfirmedEvent published", event.getOrderId());
	}

}
