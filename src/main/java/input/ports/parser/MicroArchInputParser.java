package input.ports.parser;

import input.ports.InputParserFactory;
import input.ports.InputParser;

public abstract class MicroArchInputParser{
    protected final InputParser inputParser;
    protected final Object dataExtractor;

    public MicroArchInputParser(String filePath) {
        inputParser = InputParserFactory.createParser(filePath);
        dataExtractor = createExtractor(inputParser);
    }

    protected abstract Object createExtractor(InputParser parser);
}