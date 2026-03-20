package factory;

import java.io.IOException;
import java.util.List;

public interface FileParser {
    List<String> parse(String filePath) throws IOException;
}
