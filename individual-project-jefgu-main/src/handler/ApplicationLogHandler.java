package handler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ApplicationLogHandler extends LogHandler {
    private final Map<String, Integer> applicationData = new LinkedHashMap<>();

    @Override
    public void handle(String line) {
        if (!line.contains("level=")) {
            getNext().handle(line);
        } else {
            String[] strings = line.split(" ");
            String level = null;
            for (String str : strings) {
                if (str.startsWith("level=")) {
                    level = str.split("=")[1];
                    break;
                }
            }
            if (level != null) {
                applicationData.put(level, applicationData.getOrDefault(level, 0) + 1);
            }

        }
    }

    @Override
    public void toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (FileWriter writer = new FileWriter("application.json")) {
            mapper.writeValue(writer, applicationData);
        } catch (IOException e) {
            System.out.println("Error writing Application log file: " + e.getMessage());
        }
    }
}