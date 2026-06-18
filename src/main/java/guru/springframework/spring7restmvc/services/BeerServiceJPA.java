package guru.springframework.spring7restmvc.services;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvcapi.model.BeerDTO;
import guru.springframework.spring6restmvcapi.model.BeerStyle;
import guru.springframework.spring7restmvc.repositories.BeerRepository;
import guru.springframework.spring7restmvc.events.BeerCreatedEvent;
import guru.springframework.spring7restmvc.events.BeerDeletedEvent;
import guru.springframework.spring7restmvc.events.BeerUpdatedEvent;
import guru.springframework.spring7restmvc.events.BeerPatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by jt, Spring Framework Guru.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 1000;

    void clearCache(UUID beerId){
        Optional.ofNullable(cacheManager.getCache("beerCache")).ifPresent(cache -> cache.evict(beerId));
        Optional.ofNullable(cacheManager.getCache("beerListCache")).ifPresent(cache -> cache.clear());
    }

    @Override
    @Cacheable(cacheNames = "beerListCache")
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        Page<Beer> beerPage = null;

        if(StringUtils.hasText(beerName) && beerStyle == null){
            beerPage = listBeersByName(beerName, pageRequest);
        } else if(beerStyle != null && !StringUtils.hasText(beerName)){
            beerPage = listBeersByStyle(beerStyle, pageRequest);
        } else if (beerStyle != null && StringUtils.hasText(beerName)) {
            beerPage = listBeersByNameAndStyle(beerName, beerStyle, pageRequest);
        } else {
            beerPage = beerRepository.findAll(pageRequest);
        }
        if(showInventory != null && !showInventory){
            // if inventory is not requested, set to null
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }

        return beerPage.map(beerMapper::beerToBeerDto);
        // return beerPage.stream()
        //         .map(beerMapper::beerToBeerDto)
        //         .collect(Collectors.toList());
    }

    public static PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){
        return buildPageRequest(pageNumber, pageSize, "beerName");
    }

    public static PageRequest buildPageRequest(Integer pageNumber, Integer pageSize, String sortCriteria){
        int queryPageNumber = -1;
        int queryPageSize = -1;

        if(pageNumber == null || pageNumber < 0){
            queryPageNumber = DEFAULT_PAGE_NUMBER;
        }
        else {
            // Spring Data JPA page number is 0 based index, so we need to subtract 1 from the page number
            queryPageNumber = pageNumber - 1;
        }
        if(pageSize == null || pageSize < 1){
            queryPageSize = DEFAULT_PAGE_SIZE;
        }
        else {
            if(pageSize > MAX_PAGE_SIZE){
                queryPageSize = MAX_PAGE_SIZE;
            }
            else {
                queryPageSize = pageSize;
            }
        }
        Sort sort = null;
        if(sortCriteria != null && sortCriteria.length() > 0) {
            sort = Sort.by(Sort.Order.asc(sortCriteria));
        }
        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }

    public Page<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, Pageable pageable) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageable);
    }

    public Page<Beer> listBeersByName(String beerName, Pageable pageable) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageable);
    }   

    public Page<Beer> listBeersByStyle(BeerStyle beerStyle, Pageable pageable) {
        return beerRepository.findAllByBeerStyle(beerStyle, pageable);
    }

    @Cacheable(cacheNames = "beerCache", key = "#id")
    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return Optional.ofNullable(beerMapper.beerToBeerDto(beerRepository.findById(id)
                .orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        Optional.ofNullable(cacheManager.getCache("beerListCache")).ifPresent(cache -> cache.clear());

        Beer savedBeer = beerRepository.save(beerMapper.beerDtoToBeer(beer));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        applicationEventPublisher.publishEvent(new BeerCreatedEvent(savedBeer, auth));

        return beerMapper.beerToBeerDto(savedBeer);
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();
        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            foundBeer.setBeerName(beer.getBeerName());
            foundBeer.setBeerStyle(beer.getBeerStyle());
            foundBeer.setUpc(beer.getUpc());
            foundBeer.setPrice(beer.getPrice());
            foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            foundBeer.setVersion(beer.getVersion());
            clearCache(beerId);
            Beer savedBeer = beerRepository.save(foundBeer);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            applicationEventPublisher.publishEvent(new BeerUpdatedEvent(savedBeer, auth));

            atomicReference.set(Optional.of(beerMapper.beerToBeerDto(foundBeer)));
        }, () -> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

    @Override
    public Boolean deleteById(UUID beerId) {      
        clearCache(beerId);

        if(beerRepository.existsById(beerId)){
            beerRepository.deleteById(beerId);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            applicationEventPublisher.publishEvent(new BeerDeletedEvent(
                                                    Beer.builder().id(beerId).build(), auth));

            return true;
        }
        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beer) {
        Optional<BeerDTO> result = null;
        Beer existing = beerRepository.findById(beerId).orElse(null);

        if(existing == null){
            result = Optional.empty();
        }
        else {
            clearCache(beerId);

            if (StringUtils.hasText(beer.getBeerName())) {
                existing.setBeerName(beer.getBeerName());
            }

            if (beer.getBeerStyle() != null) {
                existing.setBeerStyle(beer.getBeerStyle());
            }

            if (beer.getPrice() != null) {
                existing.setPrice(beer.getPrice());
            }

            if (beer.getQuantityOnHand() != null){
                existing.setQuantityOnHand(beer.getQuantityOnHand());
            }

            if (StringUtils.hasText(beer.getUpc())) {
                existing.setUpc(beer.getUpc());
            }

            Beer savedBeer = beerRepository.save(existing);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            applicationEventPublisher.publishEvent(new BeerPatchedEvent(savedBeer, auth));

            result = Optional.of(beerMapper.beerToBeerDto(savedBeer));
        }

        return result;
    }
}
