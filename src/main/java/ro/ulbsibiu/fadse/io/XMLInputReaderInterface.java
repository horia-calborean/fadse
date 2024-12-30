package ro.ulbsibiu.fadse.io;

import ro.ulbsibiu.fadse.environment.document.InputDocument;

public interface XMLInputReaderInterface {
    InputDocument parse(String xmlFilePath);
}
