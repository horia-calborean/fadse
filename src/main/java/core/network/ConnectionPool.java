package core.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.PreparedStatement;

/**
 * ConnectionPool verwaltet die Datenbankverbindungen, die nicht über SPRING
 * verwaltet werden.
 * <p>
 * Analog zu Beispiel:
 * <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/Semaphore.html">...</a>
 *
 * @author ralf
 */
public class ConnectionPool {

    /** Logger */
    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);
    /** Singleton-Instanz */
    private static final ConnectionPool instance = new ConnectionPool();
    private static String url = null, user = null, passwd = null;

    /** Holt singleton-Instanz */
    public static ConnectionPool getInstance() {
        return instance;
    }
    /** Maximal verfübare Anzahl an DB-Verbindungen */
    private static final int MAX_AVAILABLE = 25;

    public static void setInputDocument(Map<String, String> dbConnectionData) {
        // if the connection information in the inputDoc has changed, close all connections.
        String my_url = "jdbc:mysql://" + dbConnectionData.get("ip")
                + ":" + dbConnectionData.get("port")
                + "/" + dbConnectionData.get("name");
        String my_user = dbConnectionData.get("user");
        String my_passwd = dbConnectionData.get("password");

        if ((!Objects.equals(my_url, url))
                || (!Objects.equals(my_user, user))
                || (!Objects.equals(my_passwd, passwd))) {
            getInstance().disconnectAll();
            url = my_url;
            user = my_user;
            passwd = my_passwd;
//            log.info("Connection information has been changed to " + url + " " + user + " " + passwd);
        }
    }
    /** Array mit den DB-Verbindungen */
    protected Connection[] items = new Connection[MAX_AVAILABLE];
    /** Array mit Flags, ob DB-Verbindung momentan benutzt wird */
    protected boolean[] used = new boolean[MAX_AVAILABLE];

    /** Konstruktor */
    private ConnectionPool() {
        for (int i = 0; i < MAX_AVAILABLE; i++) {
            items[i] = null;
            used[i] = false;
        }
    }
    /** The Semaphore */
    private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);

    /** Get a conection from the pool */
    public Connection getItem() throws InterruptedException, SQLException {
//        log.debug("Grabing a connection from the pool...");
        Connection result = null;

        for (int i = 0; i < 5 && result == null; i++) {
            result = getUncheckedItem();
            if (!connectionIsValid(result)) {
                log.warn("Verbindung shall be fetched, {}th try was not successfull.", i);
                putItem(result);
                result = null;
            }
        }

        if (result == null) {
            log.error("Was not able to get a Connection!");
        }

        return result;
    }

    /**
     * A method that tests whether a connection is valid by executing
     * simple query and catch any exceptions
     */
    private boolean connectionIsValid(Connection dbConn) {
        PreparedStatement psr = null;
        boolean result = true;

        try {
            //Prepared statement is used to cache the compiled SQL
            psr = (PreparedStatement) dbConn.prepareStatement("SELECT NOW() FROM dual");
            psr.executeQuery();
        } catch (Exception e) {
            log.debug("Excpetion occured, connection is not valid: {}", e.getMessage());
            result = false;
        } finally {
            try {
                if (psr != null) {
                    psr.close();
                }
            } catch (Exception ex) {
                // log.error("Exception beim Schie�en des Test-Statements", ex);
            }
        }

        return result;
    }

    /**
     * Holt eine neue Connection aus dem ConnectionPool
     */
    private Connection getUncheckedItem() throws InterruptedException, SQLException {
//        log.info("BEFORE More DB-Connections are available: " + available.availablePermits());
        available.acquire();
//        log.info("AFTER  More DB-Connections are available: " + available.availablePermits());

        return getNextAvailableItem();
    }

    /** Give a connection back to the pool */
    public void putItem(Connection x) {
        if (x == null) {
            throw new NullPointerException("DB-Connection must not be null!");
        }

//        log.info("BEFORE More DB-Connections are available: " + available.availablePermits());
        if (markAsUnused(x)) {
            available.release();
        } else {
            log.error("Fehler beim zur�cklegen - markAsUnused meldet FALSE!");
        }
//        log.info("AFTER  More DB-Connections are available: " + available.availablePermits());
    }

    /** Get next available connection */
    protected synchronized Connection getNextAvailableItem() throws SQLException {
        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (i == 5) {
                try {
                    // TODO - Commented out by Andrei
                    //DatabaseConnector.setInstance(new DummyDatabaseConnector());
                } catch (Exception e) {
                    System.out.println("error setting DatabaseConnector instance ");
                }
            }


            if (!used[i]) {
                used[i] = true;

                return getItem(i);
            }
        }
        return null; // not reached
    }

    /** Check and if necessary initialize connection i */
    private synchronized Connection getItem(int i) throws SQLException {
        if (items[i] != null && (items[i].isClosed() || !connectionIsValid(items[i]))) {
            try {
//                log.debug("Connection " + i + " is being closed because it seems to be erronous.");
                items[i].close();
            } catch (Exception e) {
                //
            }
            items[i] = null;
        } else if (items[i] == null) {
            log.debug("Connection {} has not yet been initialized.", i);
        } else {
            log.debug("Connection {} is ready.", i);
        }

        if (items[i] == null) {
            items[i] = DriverManager.getConnection(url, user, passwd);

        }

        return items[i];
    }

    protected synchronized boolean markAsUnused(Connection item) {
        if (item == null) {
            throw new NullPointerException("DB-Connection must not be null!");
        }

        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (item == items[i]) {
                if (used[i]) {
                    used[i] = false;
//                    log.debug("Everything OK.");
                    return true;
                } else {
                    used[i] = false;
                    log.debug("Error duriong releasing, was already released.");
                    return false;
                }
            }
        }
        return false;
    }

    /** Closes all stored database connections */
    public synchronized void disconnectAll() {
//        log.info("Es werden alle Verbindungen zur DB geschlossen.");

        for (int i = 0; i < MAX_AVAILABLE; ++i) {
            if (items[i] != null) {
                try {
                    items[i].close();
                    items[i] = null;
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error while closing connection {}", i, e);
                    }
                }
            }
        }
    }
}
