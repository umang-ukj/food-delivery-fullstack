package com.fd.restaurant.dto;

import java.util.List;

import com.fd.restaurant.model.MenuItem;
import com.fd.restaurant.model.Restaurant;

public class SearchResponse {

    private String type; // RESTAURANT / MENU

    // restaurant fields
    private String id;
    private String name;
    private String location;
    //private String imageUrl;
 // only populated if menu matched
    private List<String> matchedMenus;
    
    public SearchResponse(
            String restaurantId,
            String restaurantName,
            String location,
            List<String> matchedMenus
    ) {
        this.id = restaurantId;
        this.name = restaurantName;
        this.location = location;
        this.matchedMenus = matchedMenus;
    }

    public String getRestaurantId() {
        return id;
    }

    public String getRestaurantName() {
        return name;
    }

	public String getType() {
		return type;
	}

	public String getLocation() {
		return location;
	}

	public List<String> getMatchedMenus() {
		return matchedMenus;
	}
}

