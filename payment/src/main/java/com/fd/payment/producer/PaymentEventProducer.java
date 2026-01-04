package com.fd.payment.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.PaymentEvent;


//publish result after payment is done
@Component
public class PaymentEventProducer {
	private static final Logger log =LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendPaymentResult(Long orderId, String status) {
        PaymentEvent event = new PaymentEvent(orderId, status);
        log.info("Publishing PAYMENT_{} event for orderId={}",status, orderId);

        kafkaTemplate.send("payment-events", event);
    }
}
