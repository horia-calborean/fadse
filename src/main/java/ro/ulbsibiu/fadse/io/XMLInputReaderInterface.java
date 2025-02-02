package ro.ulbsibiu.fadse.io;

import ro.ulbsibiu.fadse.environment.document.InputDocument;

public interface XMLInputReaderInterface {
    void parseSimulator();
    void parseBenchmarks();
    void parseDatabase();
    void parseMetaheuristic();
    void parseParameters();
    void parseSystemMetrics();
    void parseOutput();
}
