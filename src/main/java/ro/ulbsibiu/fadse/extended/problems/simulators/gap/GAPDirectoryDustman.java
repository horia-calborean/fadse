/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

/**
 *
 * @author jahrralf
 */
public class GAPDirectoryDustman extends LruFileCache {
    private static GAPDirectoryDustman instance = new GAPDirectoryDustman();

    public static GAPDirectoryDustman getInstance() {
        return instance;
    }

    private GAPDirectoryDustman() {
        super(3);
    }
}
