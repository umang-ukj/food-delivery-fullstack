package com.fd.restaurant.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "restaurants")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant {

    @Id
    private String id;
    @NotBlank(message = "Restaurant name is required")
    private String name;
    @NotBlank(message = "Location is required")
    private String location;
    private boolean open;
    private String imageUrl;
    private List<MenuItem> menu = new ArrayList<>();

}
