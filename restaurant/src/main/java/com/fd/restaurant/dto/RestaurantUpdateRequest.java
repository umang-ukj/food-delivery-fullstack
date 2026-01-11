package com.fd.restaurant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantUpdateRequest {
    private String name;
    private String location;
    private Boolean open;
    private String imageUrl;
}
