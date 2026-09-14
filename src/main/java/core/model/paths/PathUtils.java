package core.model.paths;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    private static final String currentDirectory = System.getProperty("user.dir");
    private static final String defaultSeparator = FileSystems.getDefault().getSeparator();

    public static String getAbsolutePath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be null or empty");
        }

        String cleanedFileName = fileName.replaceFirst("^[\\\\/]+", "");

        Path path = Paths.get(cleanedFileName).normalize();
        Path absolutePath = path.toAbsolutePath();

        return absolutePath.toString();
    }

    public static String getDseRelativePath(String fileName){
        return currentDirectory
                + defaultSeparator + "configs"
                + defaultSeparator + "designSpace"
                + defaultSeparator + fileName;
    }

    public static String getAlgorithmRelativePath(String fileName){
        return defaultSeparator + "configs" +
                defaultSeparator + "metaheuristicConfig" +
                defaultSeparator + fileName;
    }

    public static String getFadseClientsRelativePath(String fileName){
        return defaultSeparator + "configs" +
                defaultSeparator + "clients" +
                defaultSeparator + fileName;
    }
}