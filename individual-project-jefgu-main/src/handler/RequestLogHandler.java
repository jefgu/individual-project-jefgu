package handler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class RequestLogHandler extends LogHandler {
    private final Map<String, List<Integer>> responseTimes = new HashMap<>();
    private final Map<String, Map<String, Integer>> statusCounts = new HashMap<>();

    @Override
    public void handle(String line) {
        if (!line.contains("request_url=") || !line.contains("response_status=") || !line.contains("response_time_ms=")) {
            if (checkNext()) {
                return;
            }
            getNext().handle(line);
        }

        String endpoint = null;
        Integer time = null;
        String status = null;

        for (String token : line.split(" ")) {
            if (token.startsWith("request_url=")) {
                endpoint = token.split("=", 2)[1].replace("\"", "");
            } else if (token.startsWith("response_time_ms=")) {
                try {
                    time = Integer.parseInt(token.split("=", 2)[1]);
                } catch (NumberFormatException e) {
                    time = null;
                }
            } else if (token.startsWith("response_status=")) {
                String rawStatus = token.split("=", 2)[1];
                if (rawStatus.length() == 3) {
                    char codeClass = rawStatus.charAt(0);
                    status = switch (codeClass) {
                        case '2' -> "2XX";
                        case '4' -> "4XX";
                        case '5' -> "5XX";
                        default -> "OTHER";
                    };
                }
            }
        }

        if (endpoint != null && time != null && status != null) {
            responseTimes.computeIfAbsent(endpoint, k -> new ArrayList<>()).add(time);
            Map<String, Integer> counts = statusCounts.computeIfAbsent(endpoint, k -> new HashMap<>());
            counts.put(status, counts.getOrDefault(status, 0) + 1);
        }
    }

    public static int percentile(List<Integer> list, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * list.size()) - 1;
        return list.get(Math.max(0, Math.min(index, list.size() - 1)));
    }

    @Override
    public void toJSON() {
        Map<String, Object> sortedJson = new TreeMap<>();

        for (String endpoint : responseTimes.keySet()) {
            List<Integer> times = responseTimes.get(endpoint);
            if (times == null || times.isEmpty()) continue;

            Collections.sort(times);

            double[] values = times.stream().mapToDouble(i -> i).toArray();

            Percentile percentileCalculator = new Percentile().withEstimationType(Percentile.EstimationType.R_7);

            Map<String, Object> stats = new LinkedHashMap<>();  // Using LinkedHashMap to preserve insertion order
            stats.put("min", (int) values[0]);
            stats.put("50_percentile", roundPercentile(percentileCalculator.evaluate(values, 50)));
            stats.put("90_percentile", roundPercentile(percentileCalculator.evaluate(values, 90)));
            stats.put("95_percentile", roundPercentile(percentileCalculator.evaluate(values, 95)));
            stats.put("99_percentile", roundPercentile(percentileCalculator.evaluate(values, 99)));
            stats.put("max", (int) values[values.length - 1]);

            Map<String, Integer> rawStatusMap = statusCounts.getOrDefault(endpoint, new HashMap<>());
            rawStatusMap.putIfAbsent("2XX", 0);
            rawStatusMap.putIfAbsent("4XX", 0);
            rawStatusMap.putIfAbsent("5XX", 0);

            Map<String, Integer> sortedStatusCodes = new TreeMap<>(rawStatusMap);

            Map<String, Object> endpointData = new TreeMap<>();
            endpointData.put("response_times", stats);
            endpointData.put("status_codes", sortedStatusCodes);

            sortedJson.put(endpoint, endpointData);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (FileWriter writer = new FileWriter("request.json")) {
            writer.write(mapper.writeValueAsString(sortedJson));
        } catch (IOException e) {
            System.out.println("Error writing Request log file: " + e.getMessage());
        }
    }


    private double roundPercentile(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
