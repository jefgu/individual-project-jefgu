package handler;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.*;

class RequestLogHandlerTest {

    private static final String REQUEST_JSON = "request.json";

    @AfterEach
    void cleanup() {
        File file = new File(REQUEST_JSON);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testRequestLogHandler() throws IOException {
        List<String> requestLogs = List.of(
                "timestamp=2024-02-24T16:22:25Z request_method=POST request_url=\"/api/update\" response_status=202 response_time_ms=200 host=webserver1",
                "timestamp=2024-02-24T16:22:40Z request_method=GET request_url=\"/api/status\" response_status=200 response_time_ms=100 host=webserver1",
                "timestamp=2024-02-24T16:23:25Z request_method=GET request_url=\"/api/status\" response_status=200 response_time_ms=150 host=webserver1"
        );

        RequestLogHandler handler = new RequestLogHandler();
        requestLogs.forEach(handler::handle);
        handler.toJSON();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> actualJson = mapper.readValue(
                new File(REQUEST_JSON),
                Map.class
        );

        Map<String, Object> expectedJson = Map.of(
                "/api/update", Map.of(
                        "response_times", Map.of(
                                "min", 200,
                                "50_percentile", 200.0,
                                "90_percentile", 200.0,
                                "95_percentile", 200.0,
                                "99_percentile", 200.0,
                                "max", 200
                        ),
                        "status_codes", Map.of(
                                "2XX", 1,
                                "4XX", 0,
                                "5XX", 0
                        )
                ),
                "/api/status", Map.of(
                        "response_times", Map.of(
                                "min", 100,
                                "50_percentile", 125.0,
                                "90_percentile", 145.0,
                                "95_percentile", 147.5,
                                "99_percentile", 149.5,
                                "max", 150
                        ),
                        "status_codes", Map.of(
                                "2XX", 2,
                                "4XX", 0,
                                "5XX", 0
                        )
                )
        );

        assertEquals(expectedJson, actualJson);
    }
}