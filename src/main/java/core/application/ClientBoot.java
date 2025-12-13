package core.application;

import core.network.client.IndividualReceiver;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientBoot - Entry point for FADSE simulation clients.
 *
 * Starts an IndividualReceiver that listens for simulation requests from the server.
 * Includes a watchdog timer that restarts the client if no messages are received
 * for a configured timeout period (prevents hanging clients).
 *
 * Improvements:
 * - Non-daemon thread (allows simulations to complete before JVM shutdown)
 * - Proper shutdown hooks (graceful cleanup)
 * - Thread-safe state access
 * - Configurable watchdog timeout
 */
public class ClientBoot {
    private static final Logger LOGGER = Logger.getLogger(ClientBoot.class.getName());

    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "ClientBoot starting...");

        // Parse command line arguments
        int clientPort = (args.length >= 2) ? Integer.parseInt(args[1]) : 4450;

        // Create and start receiver (NON-daemon to allow simulations to complete)
        IndividualReceiver individualReceiver = new IndividualReceiver(clientPort);
        Thread receiverThread = new Thread(individualReceiver, "IndividualReceiver-Main");
        receiverThread.setDaemon(false);  // Changed to false - important!
        receiverThread.start();

        // Create watchdog scheduler
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ClientBoot-Watchdog");
            t.setDaemon(true);
            return t;
        });

        // Shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.log(Level.INFO, "Shutdown hook triggered - cleaning up...");

            // Stop receiver
            individualReceiver.shutdown();

            // Stop watchdog
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            // Wait for receiver thread to finish
            try {
                receiverThread.join(10000);  // Wait up to 10 seconds
                if (receiverThread.isAlive()) {
                    LOGGER.log(Level.WARNING, "Receiver thread did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LOGGER.log(Level.INFO, "Shutdown complete");
        }, "ClientBoot-ShutdownHook"));

        // Watchdog task: restart client if idle for too long
        Runnable watchdogTask = () -> {
            // Use thread-safe accessors
            if (!individualReceiver.isSimulating()) {
                long elapsedTimeSinceNoMessage = System.currentTimeMillis() - individualReceiver.getConnectionWaitStartTime();
                // Load watchdog timeout from configuration
                String currentDirectory = System.getProperty("user.dir");
                File dir = new File(currentDirectory);
                String fileSeparator = FileSystems.getDefault().getSeparator();
                Wini ini;
                try {
                    ini = new Wini(new File(dir + fileSeparator + "configs" + fileSeparator + "fadseConfig.ini"));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to load fadseConfig.ini - using default timeout", e);
                    return;  // Skip this watchdog check
                }

                // Get timeout in minutes from config (default: 60 minutes)
                int timeoutMinutes = ini.get("Watchdog", "time", int.class);
                if (timeoutMinutes <= 0) {
                    timeoutMinutes = 60;  // Default
                }

                long timeoutMillis = 60000L * timeoutMinutes;

                if (elapsedTimeSinceNoMessage > timeoutMillis) {
                    LOGGER.log(Level.SEVERE, String.format(
                        "Watchdog timeout: no messages received for %d minutes. Restarting client...",
                        timeoutMinutes
                    ));

                    // Gracefully shutdown receiver
                    individualReceiver.shutdown();

                    // Force exit (external process manager should restart)
                    System.exit(1);
                }
            }
        };

        // Run watchdog every 5 minutes
        int watchdogCheckMinutes = 5;
        scheduler.scheduleAtFixedRate(watchdogTask, watchdogCheckMinutes, watchdogCheckMinutes, TimeUnit.MINUTES);

        LOGGER.log(Level.INFO, String.format("ClientBoot initialized: port=%d, watchdog=%d min",
            clientPort, watchdogCheckMinutes));
    }
}