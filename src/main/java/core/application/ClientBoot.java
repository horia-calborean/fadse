package core.application;

import core.receiver.application.IndividualReceiver;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientBoot {
    public static void main(String[] args) {
        System.out.println("main(String[] args) from ClientBoot -> started");

        int clientPort = (args.length >= 2) ? Integer.parseInt(args[1]) : 4445;

        IndividualReceiver individualReceiver = new IndividualReceiver(clientPort);
        Thread receiverThread = new Thread(individualReceiver);
        receiverThread.setDaemon(true);
        receiverThread.start();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


        Runnable watchdogTask = () -> {
            if (!individualReceiver.simulating) {
                long elapsedTimeSinceNoMessage = System.currentTimeMillis() - individualReceiver.connectionWaitStartTime;
                String currentDirectory = System.getProperty("user.dir");
                File dir = new File(currentDirectory);
                String fileSeparator = FileSystems.getDefault().getSeparator();
                Wini ini;
                try {
                    ini = new Wini(new File(dir + fileSeparator + "configs" + fileSeparator + "fadseConfig.ini"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                int time = ini.get("Watchdog", "time", int.class);
                if (elapsedTimeSinceNoMessage > (60000L * time)) {
                    receiverThread.interrupt();
                    Logger.getLogger(ClientBoot.class.getName()).log(Level.SEVERE, "Watchdog had to stop this client and restart it");
                    System.exit(1);
                }
            }
        };

        int minutes = 5;
        scheduler.scheduleAtFixedRate(watchdogTask, 0, minutes, TimeUnit.MINUTES);
    }
}