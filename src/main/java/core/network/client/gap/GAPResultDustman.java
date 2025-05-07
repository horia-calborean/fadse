/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.network.client.gap;

/**
 *
 * @author jahrralf
 */
class GAPResultDustman extends LruFileCache {
    private static final GAPResultDustman instance = new GAPResultDustman();

    public static GAPResultDustman getInstance() {
        return instance;
    }

    private GAPResultDustman() {
        super(100);
    }
}
