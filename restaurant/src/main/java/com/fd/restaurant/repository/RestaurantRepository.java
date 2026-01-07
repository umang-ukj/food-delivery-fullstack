package com.fd.restaurant.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.fd.restaurant.model.MenuItem;
import com.fd.restaurant.model.Restaurant;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {

    List<Restaurant> findByLocation(String location);
    boolean existsByNameIgnoreCase(String name);
    
    @Query(value = "{}", fields = "{ 'location' : 1 }")
    List<Restaurant> findAllLocations();
    
    List<Restaurant> findByDeletedFalse();;
    
    @Query("""
    		{
    		  $or: [
    		    { name: { $regex: ?0, $options: "i" } },
    		    { "menu.name": { $regex: ?0, $options: "i" } }
    		  ]
    		}
    		""")
    		List<Restaurant> search(String query);



}
