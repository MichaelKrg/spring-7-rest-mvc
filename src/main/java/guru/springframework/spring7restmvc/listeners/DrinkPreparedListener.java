package guru.springframework.spring7restmvc.listeners;

import guru.springframework.spring7restmvc.repositories.BeerOrderRepository;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import guru.springframework.spring6restmvcapi.events.DrinkPreparedEvent;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineStatus;
import guru.springframework.spring7restmvc.config.KafkaConfig;
import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.repositories.BeerOrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class DrinkPreparedListener {
    private final BeerOrderLineRepository beerOrderLineRepository;

    @KafkaListener(groupId = "DrinkPreparedListener", topics = {KafkaConfig.DRINK_PREPARED_TOPIC})
    void listenDrinkPrepared(DrinkPreparedEvent event) {
        // we need to
        // - get the BeerOrderLineDTO from the event
        // - load the corresponding beerOrderLine from the repository (by id)
        // - update the status to COMPLETE and save it again
        beerOrderLineRepository.findById(event.getBeerOrderLine().getId())
                .ifPresentOrElse(orderLine -> {
                    orderLine.setStatus(BeerOrderLineStatus.COMPLETE);
                    beerOrderLineRepository.save(orderLine);
                    log.debug("updated status to \"COMPLETE\" for beer order line " + orderLine.getId().toString());
                },
                () -> log.error("beer order line " + event.getBeerOrderLine().getId() + " not found")
            );
    }
}
