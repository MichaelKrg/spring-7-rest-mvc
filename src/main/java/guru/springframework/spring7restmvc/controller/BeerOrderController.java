package guru.springframework.spring7restmvc.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import guru.springframework.spring6restmvcapi.model.BeerDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderCreateDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.model.BeerOrderUpdateDTO;
import guru.springframework.spring6restmvcapi.model.CustomerDTO;
import guru.springframework.spring7restmvc.services.BeerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerOrderController {
    public static final String ORDER_PATH = "/api/v1/order";
    public static final String ORDER_PATH_ID = ORDER_PATH + "/{orderId}";

    @Autowired
    private final BeerOrderService beerOrderService;

    @GetMapping(ORDER_PATH)
    public Page<BeerOrderDTO> listAllBeerOrders(@RequestParam(required = false) Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize){
        return beerOrderService.listBeerOrders(pageNumber, pageSize);
    }

    @GetMapping(value = ORDER_PATH_ID)
    public BeerOrderDTO getBeerOrderById(@PathVariable("orderId") UUID orderId){

        log.debug("Get BeerOrder by Id - in controller");

        return beerOrderService.getBeerOrderById(orderId).orElseThrow(NotFoundException::new);
    }

    @PostMapping(ORDER_PATH)
    public ResponseEntity<HttpStatus> createBeerOrder(@Validated @RequestBody BeerOrderCreateDTO beerOrder){

        BeerOrderDTO savedOrder = beerOrderService.saveNewBeerOrder(beerOrder);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", ORDER_PATH + "/" + savedOrder.getId().toString());

        return new ResponseEntity<HttpStatus>(headers, HttpStatus.CREATED);
    }

    @PutMapping(ORDER_PATH_ID)
    public ResponseEntity<HttpStatus> updateBeerOrder(@PathVariable("orderId")UUID orderId, @Validated @RequestBody BeerOrderUpdateDTO beerOrder){

        BeerOrderDTO savedOrder = beerOrderService.updateBeerOrder(orderId, beerOrder);
        return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(ORDER_PATH_ID)
    public ResponseEntity<HttpStatus> deleteById(@PathVariable("orderId") UUID orderId){

        if(! beerOrderService.deleteById(orderId)){
            throw new NotFoundException();
        }
        return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
    }
}
