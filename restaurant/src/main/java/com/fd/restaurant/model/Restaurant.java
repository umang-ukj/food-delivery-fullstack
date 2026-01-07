package com.fd.restaurant.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
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
    //textindex is mongodb specific annotation, which helps in search. 
    //even if we don't know full word and type only part of it, it returns the matches
    //adding this for our search option
    @TextIndexed
    @NotBlank(message = "Restaurant name is required")
    private String name;
    @NotBlank(message = "Location is required")
    private String location;
    private Boolean open;
    private String imageUrl;
    private boolean deleted = false;
    private List<MenuItem> menu = new ArrayList<>();

}
