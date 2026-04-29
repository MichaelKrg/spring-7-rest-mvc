package guru.springframework.spring7restmvc.services;

import java.io.File;
import java.util.List;

import guru.springframework.spring7restmvc.model.BeerCSVRecord;

public interface BeerCsvService {
    public List<BeerCSVRecord> readCSVRecords(File csvFile);
}
