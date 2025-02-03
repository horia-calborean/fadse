package ro.ulbsibiu.fadse.io.parser;

import java.util.Map;

public interface DbParser {
    Map<String, String> parseDbConnectionData();
}