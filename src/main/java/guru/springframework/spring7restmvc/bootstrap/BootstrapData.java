package guru.springframework.spring7restmvc.bootstrap;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.entities.Customer;
import guru.springframework.spring7restmvc.model.BeerCSVRecord;
import guru.springframework.spring7restmvc.model.BeerStyle;
import guru.springframework.spring7restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring7restmvc.repositories.BeerRepository;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import guru.springframework.spring7restmvc.services.BeerCsvService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jt, Spring Framework Guru.
 */
@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {
    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerCsvService beerCsvService;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        loadBeerData();
        loadCsvData();
        loadCustomerData();
        loadOrderData();
    }

    private void loadCsvData() throws FileNotFoundException{
        if(beerRepository.count() > 10){
            return;
        }
        File file = ResourceUtils.getFile("classpath:csvdata/beers.csv");

        List<BeerCSVRecord> recs = beerCsvService.readCSVRecords(file);
        recs.forEach(record -> {
            // Convert BeerStyle first since it's an enum and needs to be converted from String
            BeerStyle beerStyle = switch (record.getStyle()) {
                    case "American Pale Lager" -> BeerStyle.LAGER;
                    case "American Pale Ale (APA)", "American Black Ale", "Belgian Dark Ale", "American Blonde Ale" ->
                            BeerStyle.ALE;
                    case "American IPA", "American Double / Imperial IPA", "Belgian IPA" -> BeerStyle.IPA;
                    case "American Porter" -> BeerStyle.PORTER;
                    case "Oatmeal Stout", "American Stout" -> BeerStyle.STOUT;
                    case "Saison / Farmhouse Ale" -> BeerStyle.SAISON;
                    case "Fruit / Vegetable Beer", "Winter Warmer", "Berliner Weissbier" -> BeerStyle.WHEAT;
                    case "English Pale Ale" -> BeerStyle.PALE_ALE;
                    default -> BeerStyle.PILSNER;
                };
            // convert BeerCsvRecord to Beer entity and save to repository
            Beer beer = Beer.builder()
                    .beerName(StringUtils.abbreviate(record.getBeer(), 50)) // beerName is max 50 chars
                    .beerStyle(beerStyle)
                    .upc(record.getRow().toString())
                    .price(BigDecimal.TEN) // default price since CSV doesn't have price info
                    .quantityOnHand(record.getCount())
                    .build();
            beerRepository.save(beer);
        });
    }
    private void loadBeerData() {
        if (beerRepository.count() == 0){
            Beer beer1 = Beer.builder()
                    .beerName("Galaxy Cat")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("12356")
                    .price(new BigDecimal("12.99"))
                    .quantityOnHand(122)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            Beer beer2 = Beer.builder()
                    .beerName("Crank")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("12356222")
                    .price(new BigDecimal("11.99"))
                    .quantityOnHand(392)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            Beer beer3 = Beer.builder()
                    .beerName("Sunshine City")
                    .beerStyle(BeerStyle.IPA)
                    .upc("12356")
                    .price(new BigDecimal("13.99"))
                    .quantityOnHand(144)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            beerRepository.save(beer1);
            beerRepository.save(beer2);
            beerRepository.save(beer3);
        }

    }

    private void loadCustomerData() {

        if (customerRepository.count() == 0) {
            Customer customer1 = Customer.builder()
                    .name("Customer 1")
                    .version(1)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            Customer customer2 = Customer.builder()
                    .name("Customer 2")
                    .version(1)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            Customer customer3 = Customer.builder()
                    .name("Customer 3")
                    .version(1)
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .build();

            customerRepository.saveAll(Arrays.asList(customer1, customer2, customer3));
        }

    }

    private void loadOrderData() {
        // do that freshly only if still empty
        if(beerOrderRepository.count() == 0) {
            // we want to setup one order each for two customers.
            // Each order contains a few OrderLines each with Beer in it
            // So we
            // - take customers form customerRepository
            // - take beers from beerRepository
            // - create OrderLines adding beers
            // - create Orders adding OrderLines
            // - save the Orders in the orderRepository (with cascade save option)
            List<Customer> allCustomers = customerRepository.findAll();
            List<Beer> allBeers = beerRepository.findAll();

            Set<BeerOrderLine> orderLines1 = new HashSet<BeerOrderLine>();
            Set<BeerOrderLine> orderLines2 = new HashSet<BeerOrderLine>();

            orderLines1.add(BeerOrderLine.builder().beer(allBeers.get(0))
                            .orderQuantity(2).build());
            orderLines1.add(BeerOrderLine.builder().beer(allBeers.get(1))
                            .orderQuantity(1).build());

            orderLines2.add(BeerOrderLine.builder().beer(allBeers.get(2))
                            .orderQuantity(3).build());
            orderLines2.add(BeerOrderLine.builder().beer(allBeers.get(1))
                            .orderQuantity(10).build());

            BeerOrder beerOrder1 = BeerOrder.builder()
                                    .customer(allCustomers.get(0))
                                    .beerOrderLines(orderLines1)
                                    .build();
            BeerOrder beerOrder2 = BeerOrder.builder()
                                    .customer(allCustomers.get(1))
                                    .beerOrderLines(orderLines2)
                                    .build();
            beerOrderRepository.saveAll(Arrays.asList(beerOrder1, beerOrder2));
        }
        //List<BeerOrder> allOrders = beerOrderRepository.findAll();
    }



}
