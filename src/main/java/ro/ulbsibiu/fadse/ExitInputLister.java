package ro.ulbsibiu.fadse;




import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ralf
 */
public class ExitInputLister implements Runnable {

    public static void addExitListener() {
        ExitInputLister exi = new ExitInputLister();
        Thread t = new Thread(exi);
        t.setDaemon(true);
        t.start();
        System.out.println("The ExitInputLister is running now...");
    }

    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        try {
            InputStream in = System.in;

            while (true) {

                while (in.available() < 4) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ioe) {
                    }
                    ;
                }
                byte[] buffer = new byte[512];
                int bytesRead = in.read(buffer);
                line = new String(buffer);
                System.out.println("I read: " + line.trim());

                if (line.contains("exit")) {
                    System.out.println("###### EXIT ######");
                    System.exit(0);
                } else {
                    System.out.println("# No idea what to do with it: " + line.trim());
                }
            }

            /* while ((line = br.readLine()) != null) {
            System.out.println("A line has been read from input: " + line);

            if(line.equals("exit")) {
            System.out.println("###### EXIT ######");
            System.exit(0);
            } else {
            System.out.println("# No idea what to do with it...");
            }
            } */
        } catch (IOException ex) {
            Logger.getLogger(ExitInputLister.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                // Nothing to do.
            }
        }
        System.out.println("Exit-Listener has completed its work.");
    }
}
