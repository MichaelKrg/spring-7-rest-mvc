package guru.springframework.spring7restmvc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.model.BeerOrderLineDTO;

@Mapper
public interface BeerOrderLineMapper {
    @Mapping(target = "beer", ignore = true)
    @Mapping(target = "beerOrder", ignore = true)
    BeerOrderLine beerOrderLineDtoToBeerOrderLine(BeerOrderLineDTO dto);
    BeerOrderLineDTO beerOrderLineToBeerOrderLineDto(BeerOrderLine beer);
}
