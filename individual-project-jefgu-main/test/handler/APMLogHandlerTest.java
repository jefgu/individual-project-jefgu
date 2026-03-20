package handler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class APMLogHandlerTest {
    private static final String APM_JSON = "apm.json";

    @AfterEach
    void cleanup() {
        File outputFile = new File(APM_JSON);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    @Test
    void testAPMLogHandler() throws IOException {
        List<String> apmLogs = List.of(
                "timestamp=2024-02-24T16:22:15Z metric=cpu_usage_percent host=webserver1 value=72",
                "timestamp=2024-02-24T16:22:30Z metric=memory_usage_percent host=webserver1 value=85",
                "timestamp=2024-02-24T16:22:45Z metric=disk_usage_percent mountpoint=/ host=webserver1 value=68"
        );

        APMLogHandler handler = new APMLogHandler();
        for (String log : apmLogs) {
            handler.handle(log);
        }

        handler.toJSON();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map<String, Double>> actual = mapper.readValue(new File(APM_JSON), Map.class);

        Map<String, Map<String, Double>> expected = Map.of(
                "cpu_usage_percent", Map.of(
                        "minimum", 72.0,
                        "median", 72.0,
                        "average", 72.0,
                        "max", 72.0
                ),
                "memory_usage_percent", Map.of(
                        "minimum", 85.0,
                        "median", 85.0,
                        "average", 85.0,
                        "max", 85.0
                ),
                "disk_usage_percent", Map.of(
                        "minimum", 68.0,
                        "median", 68.0,
                        "average", 68.0,
                        "max", 68.0
                )
        );
        assertEquals(expected, actual);
    }
}