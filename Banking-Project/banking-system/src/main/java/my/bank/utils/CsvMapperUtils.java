package my.bank.utils;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CsvMapperUtils {
    private static CsvMapper csvMapper = null;

    public static CsvMapper getInstance() {
        if (csvMapper == null) {
            csvMapper = new CsvMapper();
            csvMapper.registerModule(new JavaTimeModule());
            csvMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return csvMapper;
    }
}
