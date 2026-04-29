package guru.springframework.spring7restmvc.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opencsv.bean.CsvToBeanBuilder;

import guru.springframework.spring7restmvc.model.BeerCSVRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BeerCsvServiceImpl implements BeerCsvService {
    @Override
    public List<BeerCSVRecord> readCSVRecords(File csvFile) {
        // Implementation to read CSV records using OpenCSV CSVToBeanBuilder and return a list of BeerCSVRecord objects
        try {
             List<BeerCSVRecord> records = new CsvToBeanBuilder<BeerCSVRecord>(new FileReader(csvFile))
                .withType(BeerCSVRecord.class)
                .build()
                .parse();
                return records;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("CSV file not found: " + csvFile.getAbsolutePath(), e);
        }
    }
}
