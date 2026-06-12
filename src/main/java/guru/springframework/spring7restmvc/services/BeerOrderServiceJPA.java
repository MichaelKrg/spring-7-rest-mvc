package guru.springframework.spring7restmvc.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring7restmvc.model.BeerOrderDTO;
import guru.springframework.spring7restmvc.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BeerOrderServiceJPA implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

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

}
