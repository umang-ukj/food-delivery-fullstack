package com.fd.payment.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.fd.events.PaymentEvent;
import com.fd.events.PaymentMethod;
import com.fd.events.PaymentStatus;
import com.fd.payment.entity.Payment;


//publish result after payment is done
@Component
public class PaymentEventProducer {
	private static final Logger log =LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	/*
	 * public void sendPaymentResult(Long orderId, PaymentMethod method,
	 * PaymentStatus status) { PaymentEvent event = new PaymentEvent(orderId,
	 * status, method);
	 * log.info("Publishing PAYMENT_{} event for orderId={}",status, orderId);
	 * 
	 * kafkaTemplate.send("payment-events", event); }
	 */
    
    public void publish(Payment payment) {
    	PaymentEvent event = new PaymentEvent(payment.getOrderId(),payment.getStatus(),payment.getPaymentMethod());
    	log.info("Publishing PAYMENT_{} event for orderId={}",event.getOrderId());
    	String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            kafkaTemplate.send(MessageBuilder.withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "payment-events")
                    .setHeader("X-Trace-Id", traceId)
                    .build());
            return;
        }
        kafkaTemplate.send("payment-events", event);
    }
}
