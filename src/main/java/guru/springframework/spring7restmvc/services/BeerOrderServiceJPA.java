package guru.springframework.spring7restmvc.services;

import guru.springframework.spring7restmvc.repositories.BeerRepository;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import guru.springframework.spring7restmvc.controller.NotFoundException;
import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.entities.BeerOrderShipment;
import guru.springframework.spring7restmvc.entities.Customer;
import guru.springframework.spring7restmvc.events.BeerDeletedEvent;
import guru.springframework.spring7restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import guru.springframework.spring6restmvcapi.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineUpdateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderShipmentDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderShipmentUpdateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderUpdateDTO;
import guru.springframework.spring7restmvc.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderServiceJPA implements BeerOrderService {

    private final SecurityFilterChain actuatorSecurityFilterChain;
    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Optional<BeerOrderDTO> getBeerOrderById(UUID id) {
        return Optional.ofNullable(beerOrderMapper.beerOrderToBeerOrderDto(beerOrderRepository.findById(id)
                .orElse(null)));
    }

    @Override
    public Page<BeerOrderDTO> listBeerOrders(Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = BeerServiceJPA.buildPageRequest(pageNumber, pageSize,
             "lastModifiedDate");
        Page<BeerOrder> beerPage = beerOrderRepository.findAll(pageRequest);

        return beerPage.map(beerOrderMapper::beerOrderToBeerOrderDto);
    }

    @Override
    public BeerOrderDTO saveNewBeerOrder(BeerOrderCreateDTO newBeerOrder)
    {
        // BeerOrderCreateDTO and BeerOrderLineCreateDTO as delivered contain
        // UUIDs for identifying the customer and the ordered beers
        // --> we have to load these customer / beer objects which a) verifies they
        //     exist and b) we have to send them in that way for the JPA mapping
        //     (references by UUID not enough / not what is expected)
        //     if any of the objects is missing we throw a NotFound Exception
        Customer customer = customerRepository.findById(newBeerOrder.getCustomerId())
                .orElseThrow(NotFoundException::new);

        Set<BeerOrderLine> beerOrderLines = new HashSet<>();

        newBeerOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderLines.add(BeerOrderLine.builder()
                    .beer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new))
                    .orderQuantity(beerOrderLine.getOrderQuantity())
                    .build());
        });

        BeerOrder savedBeerOrder = beerOrderRepository.save(BeerOrder.builder()
                .customer(customer)
                .beerOrderLines(beerOrderLines)
                .customerRef(newBeerOrder.getCustomerRef())
                .build());

        return beerOrderMapper.beerOrderToBeerOrderDto(savedBeerOrder);
    }
    public BeerOrderDTO updateBeerOrder(UUID orderId, BeerOrderUpdateDTO updateBeerOrder) {
        // BeerOrderUpdateDTO and BeerOrderLineUpdateDTO as delivered contain
        // UUIDs for identifying the beerOrder itself, the customer and the ordered beers
        // - we have to first load the beerOrder to be updated
        //   if it does not exist --> NotFound exception
        // - if found, we update the found object with the incoming object
        //   - Customer must be the same in any case (exception if not)
        //   - for each incoming line
        //     - if id empty --> add to collection
        //       in this case we need to load the beer object (exception if not found)
        //     - if id filled --> find and update the corresponding existing line
        //       if beerId is different: Load the beer (exception if not found)
        //       if line with given id not found --> Exception
        BeerOrder existingBeerOrder = beerOrderRepository.findById(orderId)
                                                        .orElseThrow(NotFoundException::new);
        if(!updateBeerOrder.getCustomerId().equals(existingBeerOrder.getCustomer().getId())) {
            throw new NotFoundException("Found order has different customer id which is illegal");
        }
        existingBeerOrder.setCustomerRef(updateBeerOrder.getCustomerRef());
        existingBeerOrder.setPaymentAmount(updateBeerOrder.getPaymentAmount());

        Iterator<BeerOrderLineUpdateDTO> lineIter = updateBeerOrder.getBeerOrderLines().iterator();
        while(lineIter.hasNext()) {
            BeerOrderLineUpdateDTO updateLine = lineIter.next();
            UUID updateUuid = updateLine.getId();
            if(updateUuid == null) {
                // its a new line, add it
                existingBeerOrder.getBeerOrderLines().add(BeerOrderLine.builder()
                                .beer(beerRepository.findById(updateLine.getBeerId()).orElseThrow(NotFoundException::new))
                                .orderQuantity(updateLine.getOrderQuantity())
                                .quantityAllocated(updateLine.getQuantityAllocated())
                                .build()
                            );
            }
            else {
                // find the matching line from the existing beerOrder and update it
                BeerOrderLine existingLine = existingBeerOrder.getBeerOrderLines().stream()
                        .filter(orderLine -> orderLine.getId().equals(updateUuid))
                        .findFirst()
                        .orElseThrow(NotFoundException::new);
                if(!existingLine.getBeer().getId().equals(updateLine.getBeerId())) {
                    existingLine.setBeer(beerRepository.findById(updateLine.getBeerId())
                                                    .orElseThrow(() -> new NotFoundException("Could not find beer with id "
                                                                                            + updateLine.getBeerId().toString())));
                }
                existingLine.setOrderQuantity(updateLine.getOrderQuantity());
                existingLine.setQuantityAllocated(updateLine.getQuantityAllocated());
            }
        }

        // last step: Update the Shipment information if passed in
        if(updateBeerOrder.getBeerOrderShipment() != null) {
            BeerOrderShipmentUpdateDTO updatedShipment = updateBeerOrder.getBeerOrderShipment();
            if(existingBeerOrder.getBeerOrderShipment() == null) {
                existingBeerOrder.setBeerOrderShipment(BeerOrderShipment.builder()
                                        .trackingNumber(updatedShipment.getTrackingNumber())
                                        .build()
                                    );
            }
            else {
                existingBeerOrder.getBeerOrderShipment().setTrackingNumber(
                    updatedShipment.getTrackingNumber()
                );
            }
        }
        // now the updated beerOrder is ready to be saved
        BeerOrder updatedSavedBeerOrder = beerOrderRepository.save(existingBeerOrder);
        BeerOrderDTO updatedBeerOrderDto = beerOrderMapper.beerOrderToBeerOrderDto(updatedSavedBeerOrder);

        if(updateBeerOrder.getPaymentAmount() != null) {
            applicationEventPublisher.publishEvent(OrderPlacedEvent.builder()
                .beerOrderDTO(updatedBeerOrderDto).build());
        }

        return updatedBeerOrderDto;
    }

    @Override
    public Boolean deleteById(UUID orderId) {
        if(beerOrderRepository.existsById(orderId)){
            beerOrderRepository.deleteById(orderId);

            return true;
        }
        return false;
    }
}
