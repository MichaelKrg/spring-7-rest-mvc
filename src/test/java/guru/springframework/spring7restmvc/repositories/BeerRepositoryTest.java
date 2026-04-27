package guru.springframework.spring7restmvc.repositories;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeerNameTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
            beerRepository.save(Beer.builder()
                    .beerName("My Beer with too long name 12345678901234567890") // 30 chars max
                    .beerStyle(BeerStyle.IPA)
                    .upc("not null")
                    .price(new java.math.BigDecimal("9.99"))
                    .build());
            beerRepository.flush();
        });
    }
    
    @Test
    void testSaveBeer() {
        Beer savedBeer = beerRepository.save(Beer.builder()
                        .beerName("My Beer")
                        .beerStyle(BeerStyle.IPA)
                        .upc("not null")
                        .price(new java.math.BigDecimal("9.99"))
                .build());
       beerRepository.flush();

        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }
}