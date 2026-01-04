package com.fd.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fd.user.entity.Address;
import com.fd.user.entity.User;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    Optional<Address> findByAddressIdAndUserId(String addressId, Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userId = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);

	List<Address> findByUserId(Long userId);

	List<Address> findByUserIdAndLocationIgnoreCase(Long userId, String location);
}
