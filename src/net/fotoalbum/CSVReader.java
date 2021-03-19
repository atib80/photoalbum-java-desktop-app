package net.fotoalbum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

class CSVReader {

    private final String csvFilePath;

    CSVReader(final String csvFilePath) {

        this.csvFilePath = csvFilePath;

    }

    ArrayList<String[]> getCSVData() {

        ArrayList<String[]> data = new ArrayList<>();

        try (final BufferedReader br = new BufferedReader(new FileReader(Paths.get(csvFilePath).toAbsolutePath().toFile()))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();
                String[] entries = line.split(",");
                for (int i = 0; i < entries.length; i++) entries[i] = entries[i].trim();
                data.add(entries);
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }

        return data;
    }

}
