package com.fd.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    		   select o from Order o left join fetch o.items where o.id = :id""")
    Optional<Order> findByIdWithItems(Long id);
    
    Optional<Order> findById(Long id);
    
    @Modifying @Query("""
    update Order o set o.status = com.fd.order.entity.OrderStatus.CONFIRMED
    where o.id = :orderId and o.status <> com.fd.order.entity.OrderStatus.CONFIRMED """)
    int confirmIfNotConfirmed(@Param("orderId") Long orderId);
    
    Optional<Order> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
    
    List<Order> findByUserIdAndStatusNot(Long userId, OrderStatus status);

    @Modifying
    @Query("""
        update Order o set o.status = 'CANCELLED' where o.id = :orderId and o.status not in ('CONFIRMED', 'CANCELLED')""")
    int cancelIfNotFinal(@Param("orderId") Long orderId);
    
    @Query("""
            select o from Order o where (:userId is null or o.userId = :userId)
              and (:restaurantId is null or o.restaurantId = :restaurantId) and (:status is null or o.status = :status)
              and (:orderedFrom is null or o.orderedAt >= :orderedFrom) and (:orderedTo is null or o.orderedAt <= :orderedTo)
            order by o.orderedAt desc """)
    Page<Order> findAllForAdmin(
                @Param("userId") Long userId,@Param("restaurantId") String restaurantId,@Param("status") OrderStatus status,
                @Param("orderedFrom") LocalDateTime orderedFrom,@Param("orderedTo") LocalDateTime orderedTo,Pageable pageable);
}
