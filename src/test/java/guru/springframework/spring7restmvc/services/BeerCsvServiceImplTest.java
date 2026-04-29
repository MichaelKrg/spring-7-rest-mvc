package guru.springframework.spring7restmvc.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import guru.springframework.spring7restmvc.model.BeerCSVRecord;
import static org.assertj.core.api.Assertions.assertThat;

public class BeerCsvServiceImplTest {
    private BeerCsvService beerCsvService = new BeerCsvServiceImpl();
    @Test
    void convertCSV() throws FileNotFoundException {

        File file = ResourceUtils.getFile("classpath:csvdata/beers.csv");

        List<BeerCSVRecord> recs = beerCsvService.readCSVRecords(file);

        //System.out.println(recs.size());

        assertThat(recs.size()).isGreaterThan(0);
    }
}
