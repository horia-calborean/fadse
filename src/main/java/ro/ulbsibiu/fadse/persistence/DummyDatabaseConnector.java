package ro.ulbsibiu.fadse.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.ulbsibiu.fadse.shared.statemachine.StateException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Dummy Database Connector that does nothing
 */
public class DummyDatabaseConnector extends DatabaseConnector {

    public static final String STATE_OFFLINE = "Offline";
    public static final String STATE_ONLINE = "Online";
    private Logger log = LoggerFactory.getLogger(DummyDatabaseConnector.class);
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    private static DummyDatabaseConnector instance;

    public static DummyDatabaseConnector getInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (instance == null) {
            instance = new DummyDatabaseConnector();
        }
        return instance;
    }

    /**
     * Konstruktur
     *
     * Status: Offline!
     */
    protected DummyDatabaseConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    }

    /** Aufbauen einer DB-Verbindung */
    public void connect() throws InterruptedException, SQLException, StateException {
    }

    /** Herausgeben einer Connection nach Draußen */
    public Connection getConnection() throws InterruptedException, SQLException {
        throw new SQLException("");
    }

    /**
     */
    public void disconnect() throws StateException, SQLException {
        throw new SQLException("");
    }

    /** Disconnect mit eigener Exception-Behandlung */
    public void disconnectSilently() {
    }

    /** Trennt alle Offenen DB-Verbindungen */
    public void doShutdown() {
    }

    public ResultSet executeQuery(String sqlQuery) throws SQLException, InterruptedException, StateException {
        throw new SQLException("");
    }

    public int executeUpdate(String sqlStatement) throws SQLException {
        throw new SQLException("");
    }
}
