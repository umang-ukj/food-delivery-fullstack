package com.fd.payment.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
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
    public void handleOrderEvent(OrderEvent event,@Header(name = "X-Trace-Id", required = false) String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        }
        try {
		
    	if (event.getPaymentMethod() == null) {
            log.info("Ignoring non-create order event for orderId={}", event.getOrderId());
            return;
        }
    	log.info("Received ORDER_CREATED event for orderId={}, paymentMethod={}",event.getOrderId(), event.getPaymentMethod());

    	paymentService.processPayment(event);
        } finally {
            MDC.remove("traceId");
        }
    }

}

