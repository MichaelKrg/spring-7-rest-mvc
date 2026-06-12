package guru.springframework.spring7restmvc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.model.BeerOrderDTO;

@Mapper
public interface BeerOrderMapper {
    @Mapping(target="beerOrderLines", ignore = true)
    @Mapping(target="customer", ignore = true)
    @Mapping(target="beerOrderShipment", ignore = true)
    BeerOrder beerOrderDtoToBeerOrder(BeerOrderDTO dto);
    BeerOrderDTO beerOrderToBeerOrderDto(BeerOrder beer);
}
