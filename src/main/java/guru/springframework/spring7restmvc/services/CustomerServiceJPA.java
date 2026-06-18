package guru.springframework.spring7restmvc.services;

import guru.springframework.spring7restmvc.mappers.CustomerMapper;
import guru.springframework.spring6restmvcapi.model.CustomerDTO;
import guru.springframework.spring7restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by jt, Spring Framework Guru.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CustomerServiceJPA implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CacheManager cacheManager;

    void clearCache(UUID customerId){
        Optional.ofNullable(cacheManager.getCache("customerCache")).ifPresent(cache -> cache.evict(customerId));
        Optional.ofNullable(cacheManager.getCache("customerListCache")).ifPresent(cache -> cache.clear());
    }

    @Override
    @Cacheable(cacheNames = "customerCache", key = "#customerId")
    public Optional<CustomerDTO> getCustomerById(UUID customerId) {
        log.info("Getting CustomerDTO with id: " + customerId.toString());
        return Optional.ofNullable(customerMapper
                .customerToCustomerDto(customerRepository.findById(customerId).orElse(null)));
    }

    @Override
    @Cacheable(cacheNames = "customerListCache")
    public List<CustomerDTO> getAllCustomers() {
        log.info("Getting all CustomerDTOs");
        return customerRepository.findAll().stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO saveNewCustomer(CustomerDTO customer) {
        Optional.ofNullable(cacheManager.getCache("customerListCache")).ifPresent(cache -> cache.clear());

        return customerMapper.customerToCustomerDto(customerRepository
                .save(customerMapper.customerDtoToCustomer(customer)));
    }

    @Override
    public Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customer) {
        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();
        customerRepository.findById(customerId).ifPresentOrElse(foundCustomer -> {
            clearCache(customerId);
            foundCustomer.setName(customer.getName());
            customerRepository.save(foundCustomer);
            atomicReference.set(Optional.of(customerMapper.customerToCustomerDto(foundCustomer)));
        }, () -> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

    @Override
    public Boolean deleteCustomerById(UUID customerId) {
        boolean result = false;
        if(customerRepository.existsById(customerId)){  
            clearCache(customerId);
            customerRepository.deleteById(customerId);
            result = true;
        }
        return result;
    }

    @Override
    public Optional<CustomerDTO> patchCustomerById(UUID customerId, CustomerDTO customer) {
        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();
        customerRepository.findById(customerId).ifPresentOrElse(foundCustomer -> {
            clearCache(customerId);
            if (customer.getName() != null) {
                foundCustomer.setName(customer.getName());
            }
            customerRepository.save(foundCustomer);
            atomicReference.set(Optional.of(customerMapper.customerToCustomerDto(foundCustomer)));
        }, () -> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }
}

