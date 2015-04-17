package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.shared.LruCache;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This Class provides access to the web frontend of CACTI
 * @author Ralf
 */
public class CactiConnector {

    private final static LruCache<String, String> cache = new LruCache<String, String>(256);

    /**
     * Calculates the total area in mm^2 used by a cache using the cacti 5.3 web frontend
     * For documentation: http://quid.hpl.hp.com:9081/cacti
     * @param cacheSize
     * @param lineSize
     * @param associativity
     * @param noBanks
     * @param technologyNode
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static double getArea(int cacheSize, int lineSize, int associativity, int noBanks, int technologyNode) throws MalformedURLException, IOException, Exception {
        double result = Double.NaN;

        String urlString = "";
        urlString += "http://quid.hpl.hp.com:9081/cacti/index.y";
        urlString += "?cache_size=" + cacheSize;
        urlString += "&line_size=" + lineSize;
        urlString += "&assoc=" + associativity;
        urlString += "&nrbanks=" + noBanks;
        urlString += "&technode=" + technologyNode;
        urlString += "&action=submit&cacti=cache&simple=simple_cache&preview-article=Submit";

        System.out.println("URL: " + urlString);

        String line = cache.query(urlString, 0);

        if (line == null) {
            URL url;
            BufferedReader reader = null;

            try {
                url = new URL(urlString);

                reader = new BufferedReader(new InputStreamReader(url.openStream()));

                while ((line = reader.readLine()) != null) {
                    // System.out.println(line);

                    // Now look for the interesting line...
                    if (line.contains("Total area (mm^2): ")) {
                        System.out.println("Zeile gefunden! " + line);
                        line = line.replace("Total area (mm^2): ", "");
                        line = line.replace("<br>", "");

                        System.out.println("CactiCache Miss => " + line);
                        cache.store(urlString, line);
                        break;
                    }
                }
            } catch (MalformedURLException mue) {
                throw mue;
            } catch (IOException ioe) {
                throw ioe;
            } finally {
                try {
                    reader.close();
                } catch (Exception ioe) {
                    // nothing to see here
                }
            }
        } else {
            System.out.println("CactiCache Hit => " + line);
        }

        try {
            result = Double.parseDouble(line);
        } catch (Exception e) {
            throw new Exception("Error while finding cache size, line=" + line + ", exception=" + e.getClass() + " message=" + e.getMessage());
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println("Entering main.");
        try {
            double area = CactiConnector.getArea(8192, 8 * 8, 1, 1, 90);
            System.out.println("Area: " + area);

            area = CactiConnector.getArea(8192 * 2, 8 * 8, 1, 1, 90);
            System.out.println("Area: " + area);

            area = CactiConnector.getArea(8192 * 4, 8 * 8, 1, 1, 90);
            System.out.println("Area: " + area);

            area = CactiConnector.getArea(8192, 8 * 8, 1, 1, 65);
            System.out.println("Area: " + area);

            area = CactiConnector.getArea(8192 * 2, 8 * 8, 1, 1, 65);
            System.out.println("Area: " + area);

            area = CactiConnector.getArea(8192 * 4, 8 * 8, 1, 1, 65);
            System.out.println("Area: " + area);
        } catch (Exception ex) {
            Logger.getLogger(CactiConnector.class.getName()).log(Level.SEVERE, null, ex);
        } 

        System.out.println("Exiting main.");
    }
}
