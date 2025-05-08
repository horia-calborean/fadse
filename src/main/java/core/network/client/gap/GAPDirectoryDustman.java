package core.network.client.gap;

public class GAPDirectoryDustman extends LruFileCache {
    private static final GAPDirectoryDustman instance = new GAPDirectoryDustman();

    public static GAPDirectoryDustman getInstance() {
        return instance;
    }

    private GAPDirectoryDustman() {
        super(3);
    }
}