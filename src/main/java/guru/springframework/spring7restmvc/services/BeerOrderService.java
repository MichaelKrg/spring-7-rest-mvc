package guru.springframework.spring7restmvc.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import guru.springframework.spring7restmvc.model.BeerOrderCreateDTO;
import guru.springframework.spring7restmvc.model.BeerOrderDTO;
import guru.springframework.spring7restmvc.model.BeerOrderUpdateDTO;

public interface BeerOrderService {
    Page<BeerOrderDTO> listBeerOrders(Integer pageNumber, Integer pageSize);

    Optional<BeerOrderDTO> getBeerOrderById(UUID id);
    BeerOrderDTO saveNewBeerOrder(BeerOrderCreateDTO newBeerOrder);
    BeerOrderDTO updateBeerOrder(UUID orderId, BeerOrderUpdateDTO updateBeerOrder);
    Boolean deleteById(UUID orderId);
}
