import factory.FileParser;
import factory.FileParserFactory;
import handler.*;
import java.util.List;
import java.io.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class LogParserApp {
    public static void main(String[] args) {
        if (args.length < 2 || !"--file".equals(args[0])) {
            System.out.println("Error: Format must contain --file <filename>");
            return;
        }

        String fileName = args[1];
        try {
            FileParser parser = FileParserFactory.getFileParser(fileName);
            List<String> logLines = parser.parse(fileName);

            LogHandler appHandler = new ApplicationLogHandler();
            LogHandler apmHandler = new APMLogHandler();
            LogHandler reqHandler = new RequestLogHandler();


            apmHandler.setNext(appHandler);
            appHandler.setNext(reqHandler);

            for (String line : logLines) {
                apmHandler.handle(line);
            }

            apmHandler.toJSON();
            appHandler.toJSON();
            reqHandler.toJSON();

            System.out.println("Output written to JSON files.");
        } catch (IOException e) {
            System.err.println("Error: Failed to parse file: " + e.getMessage());
        }
    }

    private static void writeJson(String filename, Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(filename), data);
        } catch (IOException e) {
            System.err.println("Error: Failed to write " + filename + ": " + e.getMessage());
        }
    }
}
