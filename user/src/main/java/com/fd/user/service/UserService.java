package com.fd.user.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fd.user.dto.AuthResponse;
import com.fd.user.dto.LoginRequest;
import com.fd.user.dto.RegisterRequest;
import com.fd.user.dto.ResetPasswordRequest;
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
    private final EmailService emailService;
    
    public UserService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil,AddressRepository addressRepo, EmailService emailService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.addressRepo=addressRepo;
        this.emailService=emailService;
    }

    public void register(RegisterRequest req) {
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(Role.user);
        //repo.save(user);
        if(repo.existsByEmail(req.getEmail())){
            throw new RuntimeException("User already exists");
        }
        User savedUser = repo.save(user);
        emailService.sendWelcomeEmail(savedUser.getEmail());
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
    @CacheEvict(value = "user-addresses",key = "#userId")
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
	
	@Cacheable(value = "user-addresses",key = "#userId")
	public List<Address> getAllForUser(Long userId) {
		//User user = repo.findById(userId).orElseThrow();

	    return addressRepo.findByUserId(userId);
	}
	
	@CacheEvict(value = "user-addresses",key = "#userId")
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
	
	@CacheEvict(value = "user-addresses",key = "#userId")
	public void deleteAddress(Long userId, String addressId) {
		Address address = addressRepo.findByAddressIdAndUserId(addressId, userId)
			    .orElseThrow(() -> new RuntimeException("Address not found"));

			if (address.getIsDefault()) {
			    throw new RuntimeException("Default address cannot be deleted");
			}

			addressRepo.delete(address);

	}

	@Transactional
	@CacheEvict(value = "user-addresses", key = "#userId")
	public void markAsDefault(Long userId, String addressId) {

	    addressRepo.clearDefaultForUser(userId);

	    Address address = addressRepo
	        .findByAddressIdAndUserId(addressId, userId)
	        .orElseThrow(() -> new RuntimeException("Address not found"));

	    address.setIsDefault(true);
	}

	public void forgotPassword(String email) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(encoder.encode(temporaryPassword));
        repo.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), temporaryPassword);
    }
	
	 public void resetPassword(ResetPasswordRequest req) {
	        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
	            throw new RuntimeException("New password and confirm password do not match");
	        }

	        User user = repo.findByEmail(req.getEmail())
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        if (!encoder.matches(req.getTemporaryPassword(), user.getPassword())) {
	            throw new RuntimeException("Invalid temporary password");
	        }

	        user.setPassword(encoder.encode(req.getNewPassword()));
	        repo.save(user);
	    }
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            builder.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }

        return builder.toString();
    }
}
