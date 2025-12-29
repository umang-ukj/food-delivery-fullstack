package com.fd.restaurant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fd.restaurant.model.Restaurant;
import com.fd.restaurant.repository.RestaurantRepository;

@SpringBootApplication
public class RestaurantApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestaurantApplication.class, args);
	}

	@Bean
	CommandLineRunner mongoCheck(RestaurantRepository repo) {
	    return args -> {
	        System.out.println("Mongo repo class: " + repo.getClass());
	        Restaurant r = new Restaurant();
	        r.setName("Mongo Proof");
	        r.setLocation("Proof");
	        r.setOpen(true);
	        repo.save(r);
	        System.out.println("Mongo save attempted");
	    };
	}
}
