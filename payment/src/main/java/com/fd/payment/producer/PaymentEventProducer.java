package com.fd.payment.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.payment.model.PaymentEvent;

//publish result after payment is done
@Component
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendPaymentResult(Long orderId, String status) {
        PaymentEvent event = new PaymentEvent(orderId, status);
        kafkaTemplate.send("payment-events", event);
    }
}
