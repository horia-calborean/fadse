package core.network;

public class IdFactory {
    private static long id = 0;
    public static synchronized String getId(){
        return ""+(id++);
    }
}