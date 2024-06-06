package org.hellonico.aiccube.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CSVToMarkdown {

    public static void main(String ...args) {
        String csvFilePrefix = "grades.csv";
        if(args.length>1) {
            csvFilePrefix = args[1];
        }
        String markdown = CSVToMarkdown.read(csvFilePrefix);
        System.out.println(markdown);
    }

    public static String read(String compressedCsvFilePath) {

        try {
            // Read the compressed CSV file
            InputStream fileStream = new FileInputStream(compressedCsvFilePath);
            //InputStream gzipStream = new GzipCompressorInputStream(fileStream);
            Reader decoder = new InputStreamReader(fileStream, StandardCharsets.UTF_8);
            BufferedReader buffered = new BufferedReader(decoder);

            // Parse the CSV file
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader()
                    .parse(buffered);

            // Store the headers and records
            List<String> headers = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            for (CSVRecord record : records) {
                if (headers.isEmpty()) {
                    record.toMap().keySet().forEach(headers::add);
                }
                List<String> row = new ArrayList<>();
                headers.forEach(header -> row.add(record.get(header)));
                rows.add(row);
            }

            // Convert to Markdown table
            String markdownTable = convertToMarkdown(headers, rows);
            return markdownTable;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String convertToMarkdown(List<String> headers, List<List<String>> rows) {
        StringBuilder markdown = new StringBuilder();

        // Append headers
        markdown.append("| ");
        headers.forEach(header -> markdown.append(header).append(" | "));
        markdown.append("\n");

        // Append separator
        markdown.append("| ");
        headers.forEach(header -> markdown.append("---").append(" | "));
        markdown.append("\n");

        // Append rows
        rows.forEach(row -> {
            markdown.append("| ");
            row.forEach(cell -> markdown.append(cell).append(" | "));
            markdown.append("\n");
        });

        return markdown.toString();
    }
}