/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.io.File;
import java.util.LinkedList;

/**
 *
 * @author jahrralf
 */
public class LruFileCache {

    private LinkedList<File> history = new LinkedList<File>();
    private int max_size = 32;

    public LruFileCache(int max_size) {
        this.max_size = max_size;
    }

    public void register(File benchmarkDirectory) {
        System.out.println(
                this.getClass().getCanonicalName() + " here, registering "
                + benchmarkDirectory);

        history.remove(benchmarkDirectory);
        history.addFirst(benchmarkDirectory);

        if (history.size() > max_size) {
            File item = history.pollLast();
            deleteFile(item);
        }
    }

    protected boolean deleteFile(File path) {
        System.out.println(
                this.getClass().getCanonicalName() + " here, starting to delete "
                + path);

        boolean result = false;
        try {
            if (path.exists() && path.isFile()) {
                result = path.delete();
            } else if (path.exists() && path.isDirectory()) {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteFile(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
                result = path.delete();
            } else {
                System.out.println(
                        this.getClass().getCanonicalName() + " here, strange situation while deleting "
                        + path);
                result = path.delete();
            }
        } catch (Exception e) {
            System.out.println(
                    this.getClass().getCanonicalName() + " here, exception while deleting "
                    + path + ": " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
