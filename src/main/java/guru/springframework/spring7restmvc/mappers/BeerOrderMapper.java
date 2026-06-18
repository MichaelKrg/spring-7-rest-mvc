package guru.springframework.spring7restmvc.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import guru.springframework.spring7restmvc.entities.BeerOrder;
import guru.springframework.spring7restmvc.entities.BeerOrderLine;
import guru.springframework.spring7restmvc.entities.BeerOrderShipment;
import guru.springframework.spring7restmvc.entities.Customer;
import guru.springframework.spring6restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderLineDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderShipmentDTO;
import guru.springframework.spring6restmvcapi.model.CustomerDTO;

@Mapper
public interface BeerOrderMapper {

    @Mapping(target = "beerOrder", ignore = true)
    BeerOrderShipment beerOrderShipmentDtoToBeerOrderShipment(BeerOrderShipmentDTO beerOrderShipmentDTO);

    BeerOrderShipmentDTO beerOrderShipmentToBeerOrderShipmentDto(BeerOrderShipment beerOrderShipment);

    @Mapping(target = "beerOrder", ignore = true)
    BeerOrderLine beerOrderLineDtoToBeerOrderLine(BeerOrderLineDTO beerOrderLineDTO);

    BeerOrderLineDTO beerOrderLineToBeerOrderLineDto(BeerOrderLine beerOrderLine);

    @Mapping(target="customer", ignore = true)
    BeerOrder beerOrderDtoToBeerOrder(BeerOrderDTO beerOrder);

    BeerOrderDTO beerOrderToBeerOrderDto(BeerOrder beerOrder);

    CustomerDTO customerToCustomerDto(Customer customer);
    //@Mapping(target="customer", ignore = true)
    //BeerOrder beerOrderDtoToBeerOrder(BeerOrderDTO dto);
    //BeerOrderDTO beerOrderToBeerOrderDto(BeerOrder beer);
}
