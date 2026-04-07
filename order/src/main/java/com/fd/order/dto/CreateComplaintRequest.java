package com.fd.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateComplaintRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    @Size(max = 120)
    private String subject;

    @NotBlank
    @Size(max = 1500)
    private String description;
}
