package com.fd.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fd.restaurant.model.MenuItem;
import com.fd.restaurant.model.Restaurant;
import com.fd.restaurant.repository.RestaurantRepository;

@Service
public class RestaurantService {

    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public Restaurant addRestaurant(Restaurant restaurant) {
        return repository.save(restaurant);
    }

    public List<Restaurant> getRestaurantsByLocation(String location) {
        return repository.findByLocation(location);
    }

    public Restaurant addMenuItem(String restaurantId, MenuItem item) {
        Restaurant restaurant = repository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.getMenu().add(item);
        return repository.save(restaurant);
    }
}

