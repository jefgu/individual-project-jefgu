package factory;

public class TxtFileParserCreator extends FileParserFactory {
    public static FileParser createParser() {
        return new TxtFileParser();
    }

}
