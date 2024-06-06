package org.hellonico.aiccube.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Utils {

    public static String fileToString(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] data = new byte[(int) file.length()];

        try(FileInputStream fis = new FileInputStream(file)) {
            fis.read(data);
            return new String(data, StandardCharsets.UTF_8);
        } finally {
            //
        }
    }


    public static List<Path> getAllFiles(String path, String ext) throws IOException {
        Path folderPath = Paths.get(path);

        try (Stream<Path> paths = Files.list(folderPath)) {
            // Filter and collect CSV files
            List<Path> csvFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("."+ext))
                    .toList();

            // Print out the CSV files
//            csvFiles.forEach(System.out::println);

            return csvFiles;
        }
    }


    public static String getBasenameWithoutExtension(Path path) {
        // Get the filename
        String filename = path.getFileName().toString();

        // Find the last dot in the filename
        int dotIndex = filename.lastIndexOf('.');

        // If there is no dot or the dot is at the start (hidden files), return the filename as is
        if (dotIndex == -1 || dotIndex == 0) {
            return filename;
        }

        // Return the substring without the extension
        return filename.substring(0, dotIndex);
    }
}
