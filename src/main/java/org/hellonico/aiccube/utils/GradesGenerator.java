package org.hellonico.aiccube.utils;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GradesGenerator {

    private static final String[] NAMES = {"Nico", "David", "Marc", "Nathalie", "Dominic"};
    private static final String[] LECTURES = {"History", "Math", "Chemistry", "Physics", "Sports", "Geography", "Biology", "Philosophy"};
    private static final String[] GRADES = {"A", "B", "C", "D", "E", "F"};
    private static final String[] SEMESTERS = {"Q1", "Q2", "Q3", "Q4"};

    public static void main(String... args) {
        int entries = 50;
        if(args.length>0) {
            try {
                entries = Integer.parseInt(args[0]);
            } finally {
                //
            }
        }
        String csvFilePrefix = "grades.csv.gz";
        if(args.length>1) {
            csvFilePrefix = args[1];
        }
        generate(entries, csvFilePrefix);
        String markdown = CSVToMarkdown.read(csvFilePrefix);
        System.out.println(markdown);
    }

    static Random random = new Random();

    public static void generate(int numEntries, String compressedCsvPath ) {
        String[] headers = {"Name", "Lecture", "Grade", "Semester"};

        // Generate random data and write to compressed CSV in one go
        try (FileOutputStream fos = new FileOutputStream(compressedCsvPath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             OutputStreamWriter osw = new OutputStreamWriter(gzos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            Random random = new Random();
            Set<String> uniqueCombinations = new HashSet<>();

            while (uniqueCombinations.size() < numEntries) {
                String name = NAMES[random.nextInt(NAMES.length)];
                String lecture = LECTURES[random.nextInt(LECTURES.length)];
                String grade = GRADES[random.nextInt(GRADES.length)];
                String semester = SEMESTERS[random.nextInt(SEMESTERS.length)];

                String combination = name + lecture + semester;

                if (!uniqueCombinations.contains(combination)) {
                    uniqueCombinations.add(combination);
                    csvPrinter.printRecord(name, lecture, grade, semester);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
