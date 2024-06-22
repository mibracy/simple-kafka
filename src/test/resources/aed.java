package com.example.fileprocessor.service;

import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileProcessorService {

    private static final String DIRECTORY = "./files";
    private static final String OUTPUT_JSON = "AEDReconcile.json";

    public void processFiles(String inputCsv) throws IOException {
        File csvFile = ResourceUtils.getFile("classpath:" + inputCsv);
        List<String> filenames = readCsvFile(csvFile);

        List<String> jsonObjects = new ArrayList<>();
        for (String filename : filenames) {
            File file = findFile(filename);
            if (file != null) {
                String creationDate = getCreationDate(file);
                String jsonObject = createJsonObject(filename, creationDate);
                jsonObjects.add(jsonObject);
            }
        }

        writeJsonOutput(jsonObjects);
    }

    private List<String> readCsvFile(File csvFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            return br.lines().collect(Collectors.toList());
        }
    }

    private File findFile(String filename) throws IOException {
        return Files.walk(Path.of(DIRECTORY))
                .filter(path -> path.getFileName().toString().equals(filename))
                .map(Path::toFile)
                .findFirst()
                .orElse(null);
    }

    private String getCreationDate(File file) throws IOException {
        Path filePath = file.toPath();
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(attrs.creationTime().toMillis());
    }

    private String createJsonObject(String filename, String creationDate) {
        return String.format("  {\n    \"filename\": \"%s\",\n    \"creation_date\": \"%s\"\n  }", filename, creationDate);
    }

    private void writeJsonOutput(List<String> jsonObjects) throws IOException {
        try (FileWriter writer = new FileWriter(OUTPUT_JSON)) {
            writer.write("[\n");
            writer.write(String.join(",\n", jsonObjects));
            writer.write("\n]");
        }
    }
}
