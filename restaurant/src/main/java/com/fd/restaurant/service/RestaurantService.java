package com.fd.restaurant.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fd.restaurant.dto.MenuItemRequest;
import com.fd.restaurant.dto.SearchResponse;
import com.fd.restaurant.model.MenuItem;
import com.fd.restaurant.model.Restaurant;
import com.fd.restaurant.repository.RestaurantRepository;

@Service
public class RestaurantService {
	
	private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);
	
    private final RestaurantRepository repository;
    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }
    
    @CacheEvict(value = { "restaurants", "restaurant-search" },allEntries = true)
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

    @CacheEvict(value = {"menu-by-restaurant","restaurant-search"},key = "#restaurantId",allEntries = false)
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
    
    @Cacheable(value = "restaurants")
	public List<Restaurant> getAllRestaurants() {
    	
    	log.info("Fetching restaurants from DB");
    	
		return repository.findAll();
	}
    
    @Cacheable(value = "menu-by-restaurant",key = "#id")
	public Restaurant getRestaurantById(String id) {
    	log.info("Fetching menu from DB for restaurantId={}", id);
		return repository.findById(id).orElseThrow(()->new RuntimeException("restaurant not found"));
	}
	
    @CacheEvict( value = "menu-by-restaurant", key = "#restaurantId")
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
	
	@CacheEvict( value = "menu-by-restaurant", key = "#restaurantId")
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
	
	@Configuration
	public class StaticResourceConfig implements WebMvcConfigurer {

	    @Override
	    public void addResourceHandlers(ResourceHandlerRegistry registry) {
	        registry.addResourceHandler("/uploads/**")
	                .addResourceLocations("file:uploads/");
	    }
	}
	
	@CacheEvict(value = { "restaurants", "restaurant-search","menu-by-restaurant"}, allEntries = true)
	@Transactional
	public void deleteRestaurant(String restaurantId) {

	    Restaurant restaurant = repository.findById(restaurantId)
	        .orElseThrow(() -> new RuntimeException("Restaurant not found"));

	    // Soft delete restaurant
	    restaurant.setDeleted(true);
	    repository.save(restaurant);

	    // Hard delete menus
	    repository.deleteById(restaurantId);

	    //log.info("Restaurant {} soft-deleted, menus removed", restaurantId);
	}
    //search service, searches for texts from both restaurants and menu db's and returns them
	@Cacheable(value = "restaurant-search",key = "#query.toLowerCase()")
	public List<SearchResponse> search(String query) {

	    List<Restaurant> restaurants = repository.search(query);

	    if (restaurants == null) {
	        return List.of();
	    }

	    return restaurants.stream()
	        .map(r -> {
	            List<String> matchedMenus = r.getMenu() == null
	                ? List.of()
	                : r.getMenu().stream()
	                    .filter(m -> m.getName().toLowerCase().contains(query.toLowerCase()))
	                    .map(MenuItem::getName)
	                    .toList();

	            return new SearchResponse(
	                r.getId(),
	                r.getName(),
	                r.getLocation(),
	                matchedMenus
	            );
	        })
	        .toList();
	}

}

