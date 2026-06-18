package guru.springframework.spring7restmvc.controller;

import guru.springframework.spring6restmvcapi.model.CustomerDTO;
import guru.springframework.spring7restmvc.services.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by jt, Spring Framework Guru.
 */

@Slf4j
@RequiredArgsConstructor
@RestController
public class CustomerController {
    public static final String CUSTOMER_PATH = "/api/v1/customer";
    public static final String CUSTOMER_PATH_ID = CUSTOMER_PATH + "/{customerId}";

    private final CustomerService customerService;

    @PatchMapping(CUSTOMER_PATH_ID)
    public ResponseEntity<HttpStatus> patchCustomerById(@PathVariable("customerId") UUID customerId,
                                                @RequestBody CustomerDTO customer){

        customerService.patchCustomerById(customerId, customer).orElseThrow(NotFoundException::new);

        return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(CUSTOMER_PATH_ID)
    public ResponseEntity<HttpStatus> deleteCustomerById(@PathVariable("customerId") UUID customerId){

        if(!customerService.deleteCustomerById(customerId)){
            throw new NotFoundException();
        }

        return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(CUSTOMER_PATH_ID)
    public ResponseEntity<HttpStatus> updateCustomerByID(@PathVariable("customerId") UUID customerId,
                                             @RequestBody CustomerDTO customer){

        customerService.updateCustomerById(customerId, customer).orElseThrow(NotFoundException::new);

        return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(CUSTOMER_PATH)
    public ResponseEntity handlePost(@RequestBody CustomerDTO customer){
        CustomerDTO savedCustomer = customerService.saveNewCustomer(customer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", CUSTOMER_PATH + "/" + savedCustomer.getId().toString());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @GetMapping(CUSTOMER_PATH)
    public List<CustomerDTO> listAllCustomers(){
        return customerService.getAllCustomers();
    }

    @GetMapping(value = CUSTOMER_PATH_ID)
    public CustomerDTO getCustomerById(@PathVariable("customerId") UUID id){
        return customerService.getCustomerById(id).orElseThrow(NotFoundException::new);
    }

}
