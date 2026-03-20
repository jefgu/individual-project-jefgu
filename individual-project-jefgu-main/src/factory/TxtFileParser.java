package factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TxtFileParser implements FileParser {
    @Override
    public List<String> parse(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
}
