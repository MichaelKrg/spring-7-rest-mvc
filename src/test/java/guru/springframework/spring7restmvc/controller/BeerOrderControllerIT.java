package guru.springframework.spring7restmvc.controller;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.model.BeerOrderDTO;
import guru.springframework.spring7restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring7restmvc.services.BeerOrderService;
import guru.springframework.spring7restmvc.services.BeerOrderServiceJPA;
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
                .andExpect(jsonPath("$.content.size()", is(2)));
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
}
