package com.fd.restaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fd.restaurant.dto.SearchResponse;
import com.fd.restaurant.model.MenuItem;
import com.fd.restaurant.model.Restaurant;
import com.fd.restaurant.repository.RestaurantRepository;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository repository;

    private RestaurantService service;

    @BeforeEach
    void setUp() {
        service = new RestaurantService(repository);
    }

    @Test
    void search_returnsOnlyOpenRestaurantsAndMatchingMenus() {
        MenuItem pizza = new MenuItem();
        pizza.setName("Paneer Pizza");
        MenuItem pasta = new MenuItem();
        pasta.setName("Creamy Pasta");

        Restaurant open = new Restaurant();
        open.setId("r1");
        open.setName("Open Spot");
        open.setLocation("Pune");
        open.setOpen(true);
        open.setMenu(List.of(pizza, pasta));

        Restaurant closed = new Restaurant();
        closed.setId("r2");
        closed.setName("Closed Spot");
        closed.setLocation("Pune");
        closed.setOpen(false);
        closed.setMenu(List.of(pizza));

        when(repository.search("pizza")).thenReturn(List.of(open, closed));

        List<SearchResponse> responses = service.search("pizza");

        assertEquals(1, responses.size());
        assertEquals("r1", responses.get(0).getRestaurantId());
        assertEquals(List.of("Paneer Pizza"), responses.get(0).getMatchedMenus());
    }

    @Test
    void addMenuItem_appliesDefaultsAndAssignsItemId() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("r-10");

        MenuItem incoming = new MenuItem();
        incoming.setName("Veg Burger");
        incoming.setPrice(120);

        when(repository.findById("r-10")).thenReturn(Optional.of(restaurant));
        when(repository.save(any(Restaurant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Restaurant saved = service.addMenuItem("r-10", incoming);

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(captor.capture());
        MenuItem added = captor.getValue().getMenu().get(0);

        assertNotNull(added.getItemId());
        assertTrue(added.getAvailable());
        assertFalse(added.getIsVeg());
        assertEquals("/images/default-food.png", added.getImageUrl());
        assertEquals(1, saved.getMenu().size());
    }
}
