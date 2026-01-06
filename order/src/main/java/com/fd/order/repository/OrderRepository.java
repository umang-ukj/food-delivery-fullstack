package com.fd.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{

    List<Order> findByUserId(Long userId);
    @Query("""
    		   select o from Order o
    		   left join fetch o.items
    		   where o.id = :id
    		""")
    		Optional<Order> findByIdWithItems(Long id);
    Optional<Order> findById(Long id);
    @Modifying
    @Query("""
    update Order o
    set o.status = com.fd.order.entity.OrderStatus.CONFIRMED
    where o.id = :orderId
      and o.status <> com.fd.order.entity.OrderStatus.CONFIRMED
    """)
    int confirmIfNotConfirmed(@Param("orderId") Long orderId);
 
    List<Order> findByUserIdAndStatusNot(Long userId, OrderStatus status);

    @Modifying
    @Query("""
        update Order o
        set o.status = 'CANCELLED'
        where o.id = :orderId
          and o.status not in ('CONFIRMED', 'CANCELLED')
    """)
    int cancelIfNotFinal(@Param("orderId") Long orderId);


}
