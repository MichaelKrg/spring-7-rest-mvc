package guru.springframework.spring7restmvc.bootstrap;

import guru.springframework.spring7restmvc.repositories.BeerRepository;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import guru.springframework.spring7restmvc.services.BeerCsvService;
import guru.springframework.spring7restmvc.services.BeerCsvServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Commit
@Import(BeerCsvServiceImpl.class)
@ActiveProfiles("localmysql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BootstrapDataTest {

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerCsvService beerCsvService;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(beerRepository, customerRepository, beerCsvService);
    }

    @Test
    void Testrun() throws Exception {
        bootstrapData.run(null);

        assertThat(beerRepository.count()).isEqualTo(2413); // 2410 from file plus 3 manual ones
        assertThat(customerRepository.count()).isEqualTo(3);
    }
}





