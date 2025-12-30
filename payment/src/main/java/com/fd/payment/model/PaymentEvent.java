package com.fd.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
//outgoing event to order service-kafka async communication
public class PaymentEvent {
    private Long orderId;
    private String status; // SUCCESS / FAILED
    
    
}
