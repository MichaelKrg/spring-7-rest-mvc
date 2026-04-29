package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring7restmvc.model.BeerDTO;
import guru.springframework.spring7restmvc.model.BeerStyle;
import guru.springframework.spring7restmvc.services.BeerService;
import guru.springframework.spring7restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
@ExtendWith(MockitoExtension.class)
class BeerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    BeerService beerService;

    BeerServiceImpl beerServiceImpl;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).get(0);

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name");

        given(beerService.patchBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(beer));

        mockMvc.perform(patch(BeerController.BEER_PATH_ID, beer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());
    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).get(0);

        given(beerService.deleteById(any(UUID.class))).willReturn(true);

        mockMvc.perform(delete(BeerController.BEER_PATH_ID, beer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(beerService).deleteById(uuidArgumentCaptor.capture());

        assertThat(beer.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testUpdateBeerFailValidations() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).get(0);
        BeerDTO newBeer = BeerDTO.builder().build();

        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(beer));
        
        // we expect 6 errors because of
        // @NotNull and @NotBlank validations on beerName and upc (2 errors each)
        // @NotNull on beerStyle and price (1 error each)
        MvcResult result = mockMvc.perform(put(BeerController.BEER_PATH_ID, beer.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBeer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
    }

    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).get(0);

        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(beer));
        
        mockMvc.perform(put(BeerController.BEER_PATH_ID, beer.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    void testCreateNewBeerFailValidations() throws Exception {
        BeerDTO beer = BeerDTO.builder().build();

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).get(1));

        // we expect 6 errors because of
        // @NotNull and @NotBlank validations on beerName and upc (2 errors each)
        // @NotNull on beerStyle and price (1 error each)
        MvcResult result = mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)))
                .andReturn();
        String responseString = result.getResponse().getContentAsString();

        beer.setBeerName("not null");
        beer.setBeerStyle(BeerStyle.PALE_ALE);
        beer.setUpc("not null");
        // price has multiple validations, test each one separately
        // 1) @Digits(integer = 4, fraction = 2) validation --> too many digits in fraction
        beer.setPrice(BigDecimal.valueOf(1.2345)); // too many digits in fraction
        result = mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();

        responseString = result.getResponse().getContentAsString();

        // 2) @Digits(integer = 4, fraction = 2) validation --> too many digits (fraction is OK)
        beer.setPrice(BigDecimal.valueOf(10000.50));
        result = mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();

        responseString = result.getResponse().getContentAsString();

        // 3) @Positive validation --> negative value
        beer.setPrice(BigDecimal.valueOf(-10.50));
        result = mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();

        responseString = result.getResponse().getContentAsString();

        // 4) @NotNull validation --> null value
        beer.setPrice(null); // Cannot be null
        result = mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)))
                .andReturn();

        responseString = result.getResponse().getContentAsString();
        //System.out.println(responseString);
    }

    @Test
    void testCreateNewBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).get(0);
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).get(1));

        mockMvc.perform(post(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testListBeers() throws Exception {
        given(beerService.listBeers(any(), any(), any(), any(), any()))
        .willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25));

        mockMvc.perform(get(BeerController.BEER_PATH)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", is(3)));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {

        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BeerController.BEER_PATH_ID, UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImpl.listBeers(null, null, false, 1, 25).get(0);

        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(get(BeerController.BEER_PATH_ID, testBeer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
    }
}