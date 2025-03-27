package core.model.paths;

import java.nio.file.FileSystems;

public class PathUtils {
    private static final String currentDirectory = System.getProperty("user.dir");
    private static final String defaultSeparator = FileSystems.getDefault().getSeparator();

    public static String getDseFullFilePath(String fileName){
        return currentDirectory
                + defaultSeparator + "configs"
                + defaultSeparator + "designSpace"
                + defaultSeparator + fileName;
    }

    public static String getAlgorithmFullFilePath(String fileName){
        return defaultSeparator + "configs" +
                defaultSeparator + "metaheuristicConfig" +
                defaultSeparator + fileName;
    }

    public static String getFadseClientsFullFilePath(String fileName){
        return defaultSeparator + "configs" +
                defaultSeparator + "clients" +
                defaultSeparator + fileName;
    }
}