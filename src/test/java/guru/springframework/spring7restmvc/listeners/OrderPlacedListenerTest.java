package guru.springframework.spring7restmvc.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import guru.springframework.spring6restmvcapi.model.BeerDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineDTO;
import guru.springframework.spring6restmvcapi.model.BeerStyle;
import guru.springframework.spring7restmvc.config.KafkaConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.greaterThan;


@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(controlledShutdown = true, topics = {KafkaConfig.ORDER_PLACED_TOPIC}, partitions = 1)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OrderPlacedListenerTest {
    static {
		System.setProperty(EmbeddedKafkaBroker.BROKER_LIST_PROPERTY, "spring.kafka.bootstrap-servers");
	}

    @Autowired
    ApplicationContext context;

    @Autowired
    KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    DrinkSplitterRouter drinkSplitter;

    @Autowired
    DrinkListenerKafkaConsumer drinkListenerKafkaConsumer;

    @Autowired
    OrderPlacedListener oderPlacedListener;

    @Autowired
    OrderPlacedKafkaListener orderPlacedKafkaListener;

    @BeforeEach
    void setUp() {
        kafkaListenerEndpointRegistry.getListenerContainers().forEach(container -> {
            ContainerTestUtils.waitForAssignment(container, 1);
        });
    }

    @Test
    void printKafkaBeans() {
        Arrays.stream(context.getBeanNamesForType(KafkaTemplate.class))
            .forEach(System.out::println);
    }

    @Test
    void testListenSplitter()  {
        drinkSplitter.receive(OrderPlacedEvent.builder()
                .beerOrderDTO(buildOrder())
                .build());

        await().atMost(15, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(drinkListenerKafkaConsumer.iceColdMessageCounter::get, greaterThan(0));

        await().atMost(15, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(drinkListenerKafkaConsumer.coldMessageCounter::get, greaterThan(0));

        await().atMost(15, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(drinkListenerKafkaConsumer.coolMessageCounter::get, greaterThan(0));
    }

    @Test
    void testListen() {
        OrderPlacedEvent orderPlacedEvent = OrderPlacedEvent.builder()
                    .beerOrderDTO(BeerOrderDTO.builder().id(UUID.randomUUID()).build())
                    .build();
        // fake order placed event
        oderPlacedListener.listen(orderPlacedEvent);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(1, orderPlacedKafkaListener.messageCounter.get());
        });
    }

    // create a test order that triggers the different router paths
    BeerOrderDTO buildOrder() {

        Set<BeerOrderLineDTO> beerOrderLines = new HashSet<>();

        beerOrderLines.add(BeerOrderLineDTO.builder()
                .beer(BeerDTO.builder()
                        .id(UUID.randomUUID())
                        .beerStyle(BeerStyle.IPA)
                        .beerName("Test Beer")
                        .build())
                .build());

        //add lager
        beerOrderLines.add(BeerOrderLineDTO.builder()
                .beer(BeerDTO.builder()
                        .id(UUID.randomUUID())
                        .beerStyle(BeerStyle.LAGER)
                        .beerName("Test Beer")
                        .build())
                .build());

        //add gose
        beerOrderLines.add(BeerOrderLineDTO.builder()
                .beer(BeerDTO.builder()
                        .id(UUID.randomUUID())
                        .beerStyle(BeerStyle.GOSE)
                        .beerName("Test Beer")
                        .build())
                .build());

        return BeerOrderDTO.builder()
                .id(UUID.randomUUID())
                .beerOrderLines(beerOrderLines)
                .build();
    }
}
