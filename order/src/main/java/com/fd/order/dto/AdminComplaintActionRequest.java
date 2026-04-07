package com.fd.order.dto;

import com.fd.order.entity.ComplaintStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminComplaintActionRequest {

    @NotNull
    private ComplaintStatus status;

    @Size(max = 1500)
    private String adminResponse;

    private boolean initiateRefund;
}
