package handler;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
public class ApplicationLogHandlerTest {

    private static final String APPLICATION_JSON = "application.json";

    @AfterEach
    void cleanup() {
        File file = new File(APPLICATION_JSON);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testApplicationHandler() throws IOException {
        List<String> applicationLogs = List.of(
                "timestamp=2024-02-24T16:22:20Z level=INFO message=\"Scheduled maintenance starting\" host=webserver1",
                "timestamp=2024-02-24T16:22:35Z level=ERROR message=\"Update process failed\" error_code=5012 host=webserver1",
                "timestamp=2024-02-24T16:22:50Z level=DEBUG message=\"Retrying update process\" attempt=1 host=webserver1"
        );

        ApplicationLogHandler handler = new ApplicationLogHandler();
        for (String log : applicationLogs) {
            handler.handle(log);
        }

        handler.toJSON();

        String content = Files.readString(new File(APPLICATION_JSON).toPath());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> actualMap = mapper.readValue(content, LinkedHashMap.class);

        Map<String, Integer> expectedMap = new LinkedHashMap<>();
        expectedMap.put("INFO", 1);
        expectedMap.put("DEBUG", 1);
        expectedMap.put("ERROR", 1);


        assertEquals(expectedMap, actualMap);
    }
}
