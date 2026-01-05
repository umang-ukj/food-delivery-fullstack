package com.fd.restaurant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fd.restaurant.dto.MenuItemRequest;
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
    	
    	if (repository.existsByNameIgnoreCase(restaurant.getName())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Restaurant already exists"
            );
        }
    	if (restaurant.getImageUrl() == null || restaurant.getImageUrl().isBlank()) {
    	    restaurant.setImageUrl("/images/default-restaurant.png");
    	}

        return repository.save(restaurant);
    }

    public List<Restaurant> getRestaurantsByLocation(String location) {
        return repository.findByLocation(location);
    }

    public Restaurant addMenuItem(String restaurantId, MenuItem item) {
    	if (item.getImageUrl() == null || item.getImageUrl().isBlank()) {
    	    item.setImageUrl("/images/default-food.png");
    	}
        Restaurant restaurant = repository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (restaurant.getMenu() == null) {
            restaurant.setMenu(new ArrayList<>());
        }
        
        item.setItemId(UUID.randomUUID().toString());
        
        restaurant.getMenu().add(item);
        return repository.save(restaurant);
    }

	public List<Restaurant> getAllRestaurants() {
		
		return repository.findAll();
	}

	public Restaurant getRestaurantById(String id) {
		
		return repository.findById(id).orElseThrow(()->new RuntimeException("restaurant not found"));
	}

	public void deleteMenuItem(String restaurantId, String itemId) {
	    Restaurant restaurant = repository.findById(restaurantId)
	            .orElseThrow(() -> new RuntimeException("Restaurant not found"));

	    boolean removed = restaurant.getMenu()
	            .removeIf(item -> itemId.equals(item.getItemId()));

	    if (!removed) {
	        throw new RuntimeException("Menu item not found");
	    }

	    repository.save(restaurant);
	}
	
	public Restaurant updateMenuItem(String restaurantId, String itemId,MenuItemRequest request) {

	    Restaurant restaurant = repository.findById(restaurantId)
	            .orElseThrow(() -> new RuntimeException("Restaurant not found"));

	    MenuItem menuItem = restaurant.getMenu().stream()
	            .filter(item -> itemId.equals(item.getItemId()))
	            .findFirst()
	            .orElseThrow(() -> new RuntimeException("Menu item not found"));

	    if (request.getPrice() != null) {
	        menuItem.setPrice(request.getPrice());
	    }

	    if (request.getAvailable() != null) {
	        menuItem.setAvailable(request.getAvailable());
	    }

	    return repository.save(restaurant);
	}
	public List<String> getAllLocations() {
	    return repository.findAll().stream()
	            .map(Restaurant::getLocation).filter(Objects::nonNull).map(String::trim)
	            .distinct().sorted().toList();
	}

}

