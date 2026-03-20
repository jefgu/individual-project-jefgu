import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;

public class LogParserAppTest {

    private static final String TEST_FILE = "test_input.txt";
    private static final String APM_JSON = "apm.json";
    private static final String APP_JSON = "application.json";
    private static final String REQ_JSON = "request.json";

    @BeforeEach
    void setup() throws IOException {
        Files.writeString(Paths.get(TEST_FILE), String.join("Test Input, to check if three log files are made if used. The log files will be empty with this test_input"));
    }

    @AfterEach
    void cleanup() {
        Stream.of(TEST_FILE, APM_JSON, APP_JSON, REQ_JSON)
                .map(File::new)
                .filter(File::exists)
                .forEach(File::delete);
    }

    @Test
    void testLogParserApp() {
        // check files are properly made from test_input.txt
        // also tests if .txt file works properly
        LogParserApp.main(new String[] { "--file", TEST_FILE });
        assertAll("Check all JSON output files",
                () -> assertTrue(new File(APM_JSON).exists(), "APM output file should exist"),
                () -> assertTrue(new File(APP_JSON).exists(), "Application log output file should exist"),
                () -> assertTrue(new File(REQ_JSON).exists(), "Request log output file should exist")
        );
    }

    @Test
    void testInvalidArgs() {
        // check if missing --file triggers error
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        LogParserApp.main(new String[] { "--wrongflag", TEST_FILE });

        System.setOut(originalOut);
        String output = out.toString();
        assertTrue(output.contains("Error: Format must contain --file"), "Should warn about invalid flag");
    }

    @Test
    void testUnsupportedFileExtension() {
        // test unsupported filetypes
        String invalidFile = "invalid_input.log";

        UnsupportedOperationException thrown = assertThrows(
                UnsupportedOperationException.class,
                () -> LogParserApp.main(new String[] { "--file", invalidFile }),
                "Expected UnsupportedOperationException for non-.txt file"
        );

        assertTrue(thrown.getMessage().contains("Unsupported file format: " + invalidFile));
    }
}
