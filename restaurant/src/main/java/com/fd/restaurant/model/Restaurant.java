package com.fd.restaurant.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "restaurants")
@Getter
@Setter
@AllArgsConstructor
public class Restaurant {

    @Id
    private String id;

    private String name;
    private String location;
    private boolean open;

    private List<MenuItem> menu;
}
