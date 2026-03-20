package factory;

public class FileParserFactory {
    public static FileParser getFileParser(String filename) {

        if (filename == null || !filename.contains(".") || filename.endsWith(".")) {
            throw new UnsupportedOperationException("Unsupported file format: " + filename);
        }

        int lastPeriod = filename.lastIndexOf('.');
        String extension = filename.substring(lastPeriod + 1).toLowerCase();

        switch (extension) {
            case "txt":
                TxtFileParserCreator txtFileParserCreator = new TxtFileParserCreator();
                return txtFileParserCreator.createParser();
            // case "other files":
            // hypothetically we would use factory method to support other log types and file formats in the future.
            // but in this case, we only allow txt file and other files are currently not supported
            default:
                throw new UnsupportedOperationException("Unsupported file format: " + filename);
        }
    }
}
