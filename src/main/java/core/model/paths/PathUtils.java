package core.model.paths;

import java.nio.file.FileSystems;

public class PathUtils {
    private static final String currentDirectory = System.getProperty("user.dir");
    private static final String defaultSeparator = FileSystems.getDefault().getSeparator();

    public static String getDseFileFullPath(String fileName){
        return currentDirectory
                + defaultSeparator + "configs"
                + defaultSeparator + "designSpace"
                + defaultSeparator + fileName;
    }

    public static String getAlgorithmFileFullPath(String fileName){
        return defaultSeparator + "configs" +
                defaultSeparator + "metaheuristicConfig" +
                defaultSeparator + fileName;
    }
}