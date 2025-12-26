package com.fd.restaurant.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fd.restaurant.model.Restaurant;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {

    List<Restaurant> findByLocation(String location);

}
