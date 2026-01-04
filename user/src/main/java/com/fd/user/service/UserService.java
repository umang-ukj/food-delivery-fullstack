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
import com.fd.user.repository.AddressRepository;
import com.fd.user.repository.UserRepository;
import com.fd.user.security.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository repo;
    private final AddressRepository addressRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil,AddressRepository addressRepo) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.addressRepo=addressRepo;
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

    public Address addAddress(Long userId, Address address) {

        // ensure user exists
        repo.findById(userId).orElseThrow();

        address.setAddressId(UUID.randomUUID().toString());
        address.setUserId(userId);
        address.setIsDefault(false);

        return addressRepo.save(address);
    }

	public List<Address> getAddressesByLocation(Long userId, String location) {
		return addressRepo.findByUserIdAndLocationIgnoreCase(userId, location);
	}

	public List<Address> getAllForUser(Long userId) {
		//User user = repo.findById(userId).orElseThrow();

	    return addressRepo.findByUserId(userId);
	}
	
	public User updateAddress(Long userId, String addressId, Address updated) {
	    User user = repo.findById(userId).orElseThrow();

	    Address addr = addressRepo.findByAddressIdAndUserId(addressId, userId)
	    	    .orElseThrow(() -> new RuntimeException("Address not found"));


	    addr.setLabel(updated.getLabel());
	    addr.setLine1(updated.getLine1());
	    addr.setLocation(updated.getLocation());
	    addr.setPincode(updated.getPincode());

	    return repo.save(user);
	}
	
	public void deleteAddress(Long userId, String addressId) {
		Address address = addressRepo.findByAddressIdAndUserId(addressId, userId)
			    .orElseThrow(() -> new RuntimeException("Address not found"));

			if (address.getIsDefault()) {
			    throw new RuntimeException("Default address cannot be deleted");
			}

			addressRepo.delete(address);

	}

	@Transactional
	public void markAsDefault(Long userId, String addressId) {

	    addressRepo.clearDefaultForUser(userId);

	    Address address = addressRepo
	        .findByAddressIdAndUserId(addressId, userId)
	        .orElseThrow(() -> new RuntimeException("Address not found"));

	    address.setIsDefault(true);
	}


}
