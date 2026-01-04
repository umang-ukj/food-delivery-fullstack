package com.fd.payment.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fd.events.OrderEvent;
import com.fd.payment.service.PaymentService;

//listen to orders
@Component
public class OrderEventConsumer {

    private final PaymentService paymentService;

	private static final Logger log =LoggerFactory.getLogger(OrderEventConsumer.class);

    public OrderEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(OrderEvent event) {
		/*
		 * if ("CREATED".equals(event.getStatus())) {
		 * //log.info("Received ORDER_CREATED event for orderId={}",
		 * event.getOrderId()); // process payment
		 * 
		 * }
		 */
    	log.info("Received ORDER_CREATED event for orderId={}, paymentMethod={}",event.getOrderId(), event.getPaymentMethod());

    	paymentService.processPayment(event);
    }

}

