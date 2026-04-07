package com.fd.order.dto;

import java.time.LocalDateTime;

import com.fd.order.entity.ComplaintStatus;
import com.fd.order.entity.OrderComplaint;

import lombok.Getter;

@Getter
public class ComplaintResponse {

    private final Long id;
    private final Long orderId;
    private final Long userId;
    private final String subject;
    private final String description;
    private final ComplaintStatus status;
    private final String adminResponse;
    private final LocalDateTime refundRequestedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ComplaintResponse(OrderComplaint complaint) {
        this.id = complaint.getId();
        this.orderId = complaint.getOrderId();
        this.userId = complaint.getUserId();
        this.subject = complaint.getSubject();
        this.description = complaint.getDescription();
        this.status = complaint.getStatus();
        this.adminResponse = complaint.getAdminResponse();
        this.refundRequestedAt = complaint.getRefundRequestedAt();
        this.createdAt = complaint.getCreatedAt();
        this.updatedAt = complaint.getUpdatedAt();
    }
}
