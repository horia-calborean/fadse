package ro.ulbsibiu.fadse;

import org.ini4j.Wini;
import ro.ulbsibiu.fadse.extended.problems.simulators.network.client.FadseClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FadseClientBoot {

    public static void main(String[] args) {
        try {
            FadseClient client;

            if (args.length < 2) {
                client = new FadseClient();
            } else {
                client = new FadseClient(Integer.parseInt(args[1]));
            }

            Thread fadseClientThread = new Thread(client);
            fadseClientThread.setDaemon(true);
            fadseClientThread.start();

            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException exception) {
                    Logger.getLogger(FadseClientBoot.class.getName()).log(Level.SEVERE, null, exception);
                }

                if (!client.isSimulating()) {
                    long millisecondsSinceNoMessage = System.currentTimeMillis() - client.getConnectionStartTime();

                    File currentDirectory = new File(System.getProperty("user.dir"));

                    String fileSeparator = FileSystems.getDefault().getSeparator();
                    Wini iniFile = new Wini(new File(currentDirectory + fileSeparator + "configs" + fileSeparator + "fadseConfig.ini"));

                    int minutesToWait = iniFile.get("Watchdog", "minutes", int.class);
                    long millisecondsToWait = 60000L * minutesToWait;

                    if (millisecondsSinceNoMessage > millisecondsToWait) {
                        fadseClientThread.interrupt();
                        Logger.getLogger(FadseClientBoot.class.getName()).log(Level.SEVERE, "Watchdog had to stop this client and restart it");
                        System.exit(1);
                    }
                }
            }
        } catch (IOException exception) {
            Logger.getLogger(FadseClientBoot.class.getName()).log(Level.SEVERE, "FADSE ini config file could not be read", exception);
        }
    }
}