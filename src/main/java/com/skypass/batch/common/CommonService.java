package com.skypass.batch.common;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CommonService {

    public void generateCsvFile(List<String[]> lines, String filePath) throws IOException {

        CSVWriter writer = new CSVWriter(new FileWriter(filePath), ICSVWriter.DEFAULT_SEPARATOR,
                ICSVWriter.NO_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END);
        writer.writeAll(lines);
        writer.close();
    }

    public LocalDateTime stringToLocalDateTime(String requestDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate ld = LocalDate.parse(requestDate, formatter);
        return LocalDateTime.of(ld, LocalDateTime.now().toLocalTime());
    }
}
