package guru.springframework.spring7restmvc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import guru.springframework.spring7restmvc.entities.BeerOrderShipment;
import guru.springframework.spring7restmvc.model.BeerOrderShipmentDTO;

@Mapper
public interface BeerOrderShipmentMapper {
    @Mapping(target = "beerOrder", ignore = true)
    BeerOrderShipment beerOrderShipmentDtoToBeerOrderShipment(BeerOrderShipmentDTO dto);
    BeerOrderShipmentDTO beerOrderShipmentToBeerOrderShipmentDto(BeerOrderShipment beer);
}
