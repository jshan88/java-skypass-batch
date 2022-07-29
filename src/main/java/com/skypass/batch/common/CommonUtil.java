package com.skypass.batch.common;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.skypass.batch.usbankCpn.CouponExtractFormat;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CommonUtil {


    public void generateCsvFileUsingOpenCsv(List<String[]> lines, String filePath) throws IOException {

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
