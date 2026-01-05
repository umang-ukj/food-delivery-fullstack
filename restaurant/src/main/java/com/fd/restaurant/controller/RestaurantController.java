package com.fd.restaurant.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fd.restaurant.config.JwtUtil;
import com.fd.restaurant.dto.MenuItemRequest;
import com.fd.restaurant.model.MenuItem;
import com.fd.restaurant.model.Restaurant;
import com.fd.restaurant.service.RestaurantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final RestaurantService service;
    private final JwtUtil jwtUtil;
    
    public RestaurantController(RestaurantService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil=jwtUtil;
    }

	/*
	 * @PostMapping public Restaurant addRestaurant(@RequestBody Restaurant
	 * restaurant) { return service.addRestaurant(restaurant); }
	 */

    @PostMapping
    public ResponseEntity<?> addRestaurant(
    		@Valid @RequestBody Restaurant restaurant,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }

        return ResponseEntity.ok(service.addRestaurant(restaurant));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<?> addMenu(
            @PathVariable String id,
            @Valid @RequestBody MenuItem item,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }

        service.addMenuItem(id, item);
        return ResponseEntity.ok("Menu added");
    }
    
    @DeleteMapping("/{id}/menu/{itemId}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }

        service.deleteMenuItem(id, itemId);
        return ResponseEntity.ok("Menu deleted");
    }
    @PutMapping("/{id}/menu/{itemId}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable String id,
            @PathVariable String itemId,
            @RequestBody MenuItemRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"admin".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }

        Restaurant updated =
                service.updateMenuItem(id, itemId, request);

        return ResponseEntity.ok(updated);
    }

    
    @GetMapping
    public List<Restaurant> getAllRestaurants(){
    	return service.getAllRestaurants();
    }
    
    @GetMapping("/{id}")
    public Restaurant getRestaurant(@PathVariable String id) {
        return service.getRestaurantById(id);
    }
    
    @GetMapping("/location")
    public List<Restaurant> getByLocation(
            @RequestParam String location) {
        return service.getRestaurantsByLocation(location);
    }
    
    @GetMapping("/locations")
    public List<String> getLocations() {
        return service.getAllLocations();
    }


	/*
	 * @PostMapping("/{id}/menu") public Restaurant addMenuItem(
	 * 
	 * @PathVariable String id,
	 * 
	 * @RequestBody MenuItem item) { return service.addMenuItem(id, item); }
	 */
    @PostMapping("/upload")
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads", fileName);

        Files.createDirectories(uploadPath.getParent());
        Files.write(uploadPath, file.getBytes());

        return Map.of(
            "imageUrl", "/uploads/" + fileName
        );
    }
}
