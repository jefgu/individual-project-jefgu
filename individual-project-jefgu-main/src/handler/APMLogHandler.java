package handler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class APMLogHandler extends LogHandler {
    private final Map<String, List<Double>> apmData = new HashMap<>();

    @Override
    public void handle(String line) {
        if (!line.contains("metric=") || !line.contains("value=")) {
            if (checkNext()) {
                return;
            }
            getNext().handle(line);
        }

        String metric = null;
        Double value = null;

        for (String token : line.split(" ")) {
            if (token.startsWith("metric=")) {
                metric = token.split("=")[1];
            } else if (token.startsWith("value=")) {
                value = Double.parseDouble(token.split("=")[1]);
            }
        }

        if (metric != null && value != null) {
            apmData.computeIfAbsent(metric, k -> new ArrayList<>()).add(value);
        }
    }

    @Override
    public void toJSON() {
        Map<String, Map<String, Double>> aggregation = new TreeMap<>();

        for (String metric : apmData.keySet()) {
            List<Double> values = apmData.get(metric);
            if (values.isEmpty()) continue;

            Collections.sort(values);

            double min = values.get(0);
            double max = values.get(values.size() - 1);
            double sum = 0.0;
            for (double val : values) {
                sum += val;
            }
            double avg = sum / values.size();

            double median;
            int size = values.size();
            if (size % 2 == 0) {
                median = (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
            } else {
                median = values.get(size / 2);
            }

            Map<String, Double> statMap = new LinkedHashMap<>();
            statMap.put("minimum", min);
            statMap.put("median", median);
            statMap.put("average", avg);
            statMap.put("max", max);

            aggregation.put(metric, statMap);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (FileWriter writer = new FileWriter("apm.json")) {
            writer.write(mapper.writeValueAsString(aggregation));
        } catch (IOException e) {
            System.out.println("Error writing APM log file: " + e.getMessage());
        }
    }
}