package com.example.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import com.example.delivery.model.Delivery;
import com.example.delivery.producer.DeliveryEventProducer;
import com.example.delivery.repository.DeliveryRepository;
import com.fd.events.DeliveryEvent;
import com.fd.events.DeliveryStatus;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private DeliveryEventProducer deliveryProducer;
    @Mock
    private DeliveryAsyncService asyncService;

    private DeliveryService service;

    @BeforeEach
    void setUp() {
        service = new DeliveryService(deliveryRepository, deliveryProducer, asyncService);
    }

    @Test
    void createDelivery_whenAlreadyExists_isNoOp() {
        when(deliveryRepository.findFirstByOrderId(100L)).thenReturn(Optional.of(new Delivery()));

        service.createDelivery(100L);

        verify(deliveryRepository, never()).save(any(Delivery.class));
        verify(deliveryProducer, never()).publish(any(DeliveryEvent.class));
        verify(asyncService, never()).runDeliveryFlow(any(Long.class), any());
    }

    @Test
    void createDelivery_newRecord_savesPublishesAndStartsAsyncFlow() {
        when(deliveryRepository.findFirstByOrderId(200L)).thenReturn(Optional.empty());

        MDC.put("traceId", "trace-1");
        try {
            service.createDelivery(200L);
        } finally {
            MDC.clear();
        }

        ArgumentCaptor<Delivery> deliveryCaptor = ArgumentCaptor.forClass(Delivery.class);
        verify(deliveryRepository).save(deliveryCaptor.capture());
        assertEquals(200L, deliveryCaptor.getValue().getOrderId());
        assertEquals(DeliveryStatus.CREATED, deliveryCaptor.getValue().getStatus());

        ArgumentCaptor<DeliveryEvent> eventCaptor = ArgumentCaptor.forClass(DeliveryEvent.class);
        verify(deliveryProducer).publish(eventCaptor.capture());
        assertEquals(DeliveryStatus.CREATED, eventCaptor.getValue().getStatus());

        verify(asyncService).runDeliveryFlow(200L, "trace-1");
    }

    @Test
    void cancelDelivery_whenInProgress_marksCancelledAndPublishes() {
        Delivery existing = new Delivery();
        existing.setOrderId(300L);
        existing.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);

        when(deliveryRepository.findFirstByOrderId(300L)).thenReturn(Optional.of(existing));

        service.cancelDelivery(300L);

        verify(deliveryRepository).save(existing);
        assertEquals(DeliveryStatus.CANCELLED, existing.getStatus());
        verify(deliveryProducer).publish(any(DeliveryEvent.class));
    }

    @Test
    void cancelDelivery_whenAlreadyDelivered_doesNotPublishCancellation() {
        Delivery delivered = new Delivery();
        delivered.setOrderId(400L);
        delivered.setStatus(DeliveryStatus.DELIVERED);

        when(deliveryRepository.findFirstByOrderId(400L)).thenReturn(Optional.of(delivered));

        service.cancelDelivery(400L);

        verify(deliveryRepository, never()).save(any(Delivery.class));
        verify(deliveryProducer, never()).publish(any(DeliveryEvent.class));
    }
}
