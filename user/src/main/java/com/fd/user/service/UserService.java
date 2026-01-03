package com.fd.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fd.user.dto.AuthResponse;
import com.fd.user.dto.LoginRequest;
import com.fd.user.dto.RegisterRequest;
import com.fd.user.entity.Address;
import com.fd.user.entity.Role;
import com.fd.user.entity.User;
import com.fd.user.repository.UserRepository;
import com.fd.user.security.JwtUtil;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest req) {
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(Role.user);
        repo.save(user);
    }

    public AuthResponse login(LoginRequest req) {
    	
        User user = repo.findByEmail(req.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token,user.getRole().name());
    }

    public User addAddress(Long userId, Address address) {
        User user = repo.findById(userId).orElseThrow();
        address.setAddressId(UUID.randomUUID().toString());
        user.getAddresses().add(address);
        return repo.save(user);
    }

	public List<Address> getAddressesByLocation(Long userId, String location) {
	    User user = repo.findById(userId).orElseThrow();

	    return user.getAddresses().stream()
	        .filter(a -> a.getLocation().equalsIgnoreCase(location))
	        .toList();
	}

	public User updateAddress(Long userId, String addressId, Address updated) {
	    User user = repo.findById(userId).orElseThrow();

	    Address addr = user.getAddresses().stream()
	        .filter(a -> a.getAddressId().equals(addressId))
	        .findFirst()
	        .orElseThrow(() -> new RuntimeException("Address not found"));

	    addr.setLabel(updated.getLabel());
	    addr.setLine1(updated.getLine1());
	    addr.setLocation(updated.getLocation());
	    addr.setPincode(updated.getPincode());

	    return repo.save(user);
	}
	
	public User deleteAddress(Long userId, String addressId) {
	    User user = repo.findById(userId).orElseThrow();

	    user.getAddresses().removeIf(a -> a.getAddressId().equals(addressId));
	    return repo.save(user);
	}

}
