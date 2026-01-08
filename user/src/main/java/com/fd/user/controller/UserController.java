package com.fd.user.controller;

import java.util.List;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.fd.user.dto.AuthResponse;
import com.fd.user.dto.LoginRequest;
import com.fd.user.dto.RegisterRequest;
import com.fd.user.entity.Address;
import com.fd.user.entity.User;
import com.fd.user.security.JwtUtil;
import com.fd.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Logger log=LoggerFactory.getLogger(UserController.class);
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil=jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register( @Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public AuthResponse login( @Valid @RequestBody LoginRequest req) {
    	log.info("ELK_TEST: service started successfully");

        return userService.login(req);
    }
    
    @PostMapping("/addresses")
    public Address addAddress(
        @RequestHeader("Authorization") String auth,
        @Valid @RequestBody Address address
    ) {
        Long userId = JwtUtil.extractUserId(auth.substring(7));
        return userService.addAddress(userId, address);
    }

    @GetMapping("/addresses")
    public List<Address> getAddresses(
        @RequestHeader("Authorization") String auth,
        @RequestParam String location
    ) {
        Long userId = JwtUtil.extractUserId(auth.substring(7));
        return userService.getAddressesByLocation(userId, location);
    }
    
    @GetMapping("/addresses/all")
    public List<Address> getAllUserAddresses(@RequestHeader("Authorization") String auth) {
    	Long userId = JwtUtil.extractUserId(auth.substring(7));
        return userService.getAllForUser(userId);
    }
    
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<?> updateAddress(@RequestHeader("Authorization") String auth, @PathVariable String addressId,
    		@Valid @RequestBody Address address) {
    	
        Long userId = JwtUtil.extractUserId(auth.substring(7));
        userService.updateAddress(userId, addressId, address);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<?> deleteAddress(@RequestHeader("Authorization") String auth,@PathVariable String addressId) {
        Long userId = JwtUtil.extractUserId(auth.substring(7));
         userService.deleteAddress(userId, addressId);
         return ResponseEntity.ok().build();
    }
    
    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<?> markDefault(@RequestHeader("Authorization") String auth, @PathVariable String addressId) {
        Long userId = JwtUtil.extractUserId(auth.substring(7));

        userService.markAsDefault(userId, addressId);
        return ResponseEntity.ok().build();
    }

}
