package ro.ulbsibiu.fadse.io.parser;

import ro.ulbsibiu.fadse.environment.Objective;

import java.util.Map;

public interface SystemMetricParser {
    Map<String, Objective> parseObjectives();
}