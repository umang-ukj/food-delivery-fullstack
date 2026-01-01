package com.fd.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fd.order.entity.Order;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

    List<Order> findByUserId(Long userId);
    @Query("""
    		   select o from Order o
    		   left join fetch o.items
    		   where o.id = :id
    		""")
    		Optional<Order> findByIdWithItems(Long id);

}
