package guru.springframework.spring7restmvc.services;

import guru.springframework.spring7restmvc.entities.Beer;
import guru.springframework.spring7restmvc.mappers.BeerMapper;
import guru.springframework.spring7restmvc.model.BeerDTO;
import guru.springframework.spring7restmvc.model.BeerStyle;
import guru.springframework.spring7restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by jt, Spring Framework Guru.
 */
@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 1000;

    @Override
    public List<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);
        List<Beer> beerList = null;

        if(StringUtils.hasText(beerName) && beerStyle == null){
            beerList = listBeersByName(beerName);
        } else if(beerStyle != null && !StringUtils.hasText(beerName)){
            beerList = listBeersByStyle(beerStyle);
        } else if (beerStyle != null && StringUtils.hasText(beerName)) {
            beerList = listBeersByNameAndStyle(beerName, beerStyle);
        } else {
            beerList = beerRepository.findAll();
        }
        if(showInventory != null && !showInventory){
            // if inventory is not requested, set to null
            beerList.forEach(beer -> beer.setQuantityOnHand(null));
        }
        return beerList.stream()
                .map(beerMapper::beerToBeerDto)
                .collect(Collectors.toList());
    }

    public PageRequest buildPageRequest(Integer pageNumber, Integer pageSize){
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
        return PageRequest.of(queryPageNumber, queryPageSize);
    }

    public List<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle);
    }

    public List<Beer> listBeersByName(String beerName) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%");
    }   

    public List<Beer> listBeersByStyle(BeerStyle beerStyle) {
        return beerRepository.findAllByBeerStyle(beerStyle);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return Optional.ofNullable(beerMapper.beerToBeerDto(beerRepository.findById(id)
                .orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
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
            beerRepository.save(foundBeer);
            atomicReference.set(Optional.of(beerMapper.beerToBeerDto(foundBeer)));
        }, () -> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

    @Override
    public Boolean deleteById(UUID beerId) {
        if(beerRepository.existsById(beerId)){
            beerRepository.deleteById(beerId);
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
            result = Optional.of(beerMapper.beerToBeerDto(beerRepository.save(existing)));
        }

        return result;
    }
}
