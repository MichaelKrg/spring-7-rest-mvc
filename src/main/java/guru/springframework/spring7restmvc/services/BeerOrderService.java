package guru.springframework.spring7restmvc.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import guru.springframework.spring7restmvc.model.BeerOrderDTO;

public interface BeerOrderService {
    Page<BeerOrderDTO> listBeerOrders(Integer pageNumber, Integer pageSize);

    Optional<BeerOrderDTO> getBeerOrderById(UUID id);
}
