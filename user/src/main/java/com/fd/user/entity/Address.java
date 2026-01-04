package com.fd.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "addresses")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Address {
	@Id
	@Column(nullable = false, unique = true)
	private String addressId ;
	
	@Column(name = "user_id")
    private Long userId;
	
	@NotBlank(message = "please set a label")
    private String label;    
	
	@NotBlank(message = "please enter an address")
    private String line1;
    
    @NotBlank(message = "please enter a location")
    private String location;     
    
    @Pattern(regexp = "\\d{6}", message = "Invalid pincode")
    private String pincode;

    @Column(name = "is_default")
    private Boolean isDefault;
    
}
