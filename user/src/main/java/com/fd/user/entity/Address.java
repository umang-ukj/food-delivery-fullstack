package com.fd.user.entity;

import java.util.UUID;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.NoArgsConstructor;
@Embeddable
@NoArgsConstructor
public class Address {
	//safe edit/delete instead of using list index
	private String addressId = UUID.randomUUID().toString();
	
	@NotBlank(message = "please set a label")
    private String label;    
	
	@NotBlank(message = "please enter an address")
    private String line1;
    
    @NotBlank(message = "please enter a location")
    private String location;     
    
    @Pattern(regexp = "\\d{6}", message = "Invalid pincode")
    private String pincode;

	public String getAddressId() {
		return addressId;
	}

	public void setAddressId(String addressId) {
		this.addressId = addressId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public Address(String addressId, @NotBlank(message = "please set a label") String label,
			@NotBlank(message = "please enter an address") String line1,
			@NotBlank(message = "please enter a location") String location,
			@NotBlank(message = "pincode required") String pincode) {
		super();
		this.addressId = addressId;
		this.label = label;
		this.line1 = line1;
		this.location = location;
		this.pincode = pincode;
	}
    
    
}
