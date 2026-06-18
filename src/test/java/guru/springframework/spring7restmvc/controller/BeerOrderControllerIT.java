package guru.springframework.spring7restmvc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import guru.springframework.spring6restmvcapi.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineCreateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineUpdateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderShipmentUpdateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderUpdateDTO;
import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.events.BeerDeletedEvent;
import guru.springframework.spring7restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring7restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring7restmvc.repositories.BeerRepository;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import guru.springframework.spring7restmvc.services.BeerOrderService;
import guru.springframework.spring7restmvc.services.BeerOrderServiceJPA;
import jakarta.transaction.Transactional;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
public class BeerOrderControllerIT {
    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Autowired
    JsonMapper objectMapper;

    @Autowired
    BeerOrderRepository beerOrderRepository;
    @Autowired
    BeerOrderMapper beerOrderMapper;

    @Autowired
    BeerRepository beerRepository;
    @Autowired
    CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
         .build();
    }

    @Test
    void testListAllBeerOrders() throws Exception {
        mockMvc.perform(get(BeerOrderController.ORDER_PATH)
                .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()", greaterThanOrEqualTo(2)));
    }

    @Test
    void testGetBeerOrderById() throws Exception {
        BeerOrder testOrder = beerOrderRepository.findAll().get(0);
        
        mockMvc.perform(get(BeerOrderController.ORDER_PATH_ID, testOrder.getId())
                .with(BeerControllerTest.jwtRequestPostProcessor)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testOrder.getId().toString())));
    }

    @Test
    void testCreateNewBeerOrder() throws Exception {
        // first get a Customer and a beer for creating the order
        UUID beerId = beerRepository.findAll().get(0).getId();
        UUID customerId = customerRepository.findAll().get(0).getId();

        // Create a BeerOrderLineCreateDTO Set we can add to the newly created order
        Set<BeerOrderLineCreateDTO> bolSet = new HashSet<>();
        bolSet.add(BeerOrderLineCreateDTO.builder()
                    .orderQuantity(7)
                    .beerId(beerId)
                    .build());

        // create the new order
        BeerOrderCreateDTO testOrderDTO = BeerOrderCreateDTO.builder()
                        .customerId(customerId)
                        .customerRef("newOrder")
                        .beerOrderLines(bolSet)
                        .build();
        
        mockMvc.perform(post(BeerOrderController.ORDER_PATH)
                .with(BeerControllerTest.jwtRequestPostProcessor)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Transactional
    @Test
    void testUpdateBeerOrder() throws Exception {
        UUID beerId3 = beerRepository.findAll().get(3).getId();
        // first get an existing beerOrder
        BeerOrder existingBeerOrder = beerOrderRepository.findAll().get(0);

        // we want to create a test that
        // - updates the customerRef
        // - update the order lines
        //   - updates an existing orderLine (the first one the iterator finds)
        //   - leave other existing order lines unchanged
        //   - adds a new order line
        //   - ### later: deletes an order line (set order amount to 0?)
        // - update shipping info (add trackiong number)
        HashSet<BeerOrderLineUpdateDTO> updateOrderLines = new HashSet<>();
        BeerOrderLine bol = existingBeerOrder.getBeerOrderLines().iterator().next();
        updateOrderLines.add(BeerOrderLineUpdateDTO.builder()
                            .id(bol.getId())
                            .beerId(bol.getBeer().getId())
                            // + 1 to the quantity for the update
                            .orderQuantity(bol.getOrderQuantity().intValue() + 1)
                            .build()
                        );
        updateOrderLines.add(BeerOrderLineUpdateDTO.builder()
                            .beerId(beerId3)
                            .orderQuantity(33)
                            .build()
                        );
        BeerOrderUpdateDTO beerOrderUpdate = BeerOrderUpdateDTO.builder()
                .id(existingBeerOrder.getId())
                .customerId(existingBeerOrder.getCustomer().getId())
                .customerRef("newCutomerRef")
                .beerOrderShipment(BeerOrderShipmentUpdateDTO.builder().trackingNumber("12345").build())
                .beerOrderLines(updateOrderLines)
                .build();
        
        mockMvc.perform(put(BeerOrderController.ORDER_PATH_ID, beerOrderUpdate.getId())
                .with(BeerControllerTest.jwtRequestPostProcessor)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerOrderUpdate)))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteById() throws Exception {
        BeerOrder beerOrder = beerOrderRepository.findAll().get(0);

        mockMvc.perform(delete(BeerOrderController.ORDER_PATH_ID, beerOrder.getId())
                            .with(BeerControllerTest.jwtRequestPostProcessor))
                            .andExpect(status().isNoContent());

        assertTrue(beerOrderRepository.findById(beerOrder.getId()).isEmpty());
    }

    @Test
    void testDeleteByIdNotFound() throws Exception {
        mockMvc.perform(delete(BeerOrderController.ORDER_PATH_ID, UUID.randomUUID())
                            .with(BeerControllerTest.jwtRequestPostProcessor))
                            .andExpect(status().isNotFound());
    }

}
