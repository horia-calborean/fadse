package ro.ulbsibiu.fadse.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ulbsibiu.fadse.shared.statemachine.StateException;
import ro.ulbsibiu.fadse.shared.statemachine.StateMachine;

/**
 * Klasse f�r den Zugriff auf eine MySQL-DB
 */
public class DatabaseConnector extends StateMachine {

    public static final String STATE_OFFLINE = "Offline";
    public static final String STATE_ONLINE = "Online";
    private Logger log = LoggerFactory.getLogger(DatabaseConnector.class);
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    private static DatabaseConnector instance;

    public static DatabaseConnector getInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    /**
     * Konstruktur
     *
     * Status: Offline!
     */
    protected DatabaseConnector() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (log.isDebugEnabled()) {
            log.debug("######################  MysqlDatenspeicher erzeugen");
        }

        setState(STATE_OFFLINE);
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }

    /** Aufbauen einer DB-Verbindung */
    public void connect() throws InterruptedException, SQLException, StateException {
        assertState(STATE_OFFLINE);

        con = ConnectionPool.getInstance().getItem();
        stmt = con.createStatement();

        setState(STATE_ONLINE);
    }

    /** Herausgeben einer Connection nach Draußen */
    public Connection getConnection() throws InterruptedException, SQLException {
        return ConnectionPool.getInstance().getItem();
    }

    /**
     * Trennt die Verbindung zur DB
     * @throws zugang.hardware.DatenSpeicherException
     */
    public void disconnect() throws StateException, SQLException {
        assertState(STATE_ONLINE);

        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ex) {
            if (log.isDebugEnabled()) {
                log.debug("FEHLER BEIM DISCONNECT");
            }
            throw ex;
        } finally {
            ConnectionPool.getInstance().putItem(con);

            rs = null;
            stmt = null;
            con = null;

            if (log.isDebugEnabled()) {
                log.debug("Disconnect beendet...");
            }
            setState(STATE_OFFLINE);
        }
    }

    /** Disconnect mit eigener Exception-Behandlung */
    public void disconnectSilently() {
        try {
            disconnect();
        } catch (Exception ex) {
            log.warn("Fehler beim Trennen der DB-Verbindung", ex);
        }
    }

    /** Trennt alle Offenen DB-Verbindungen */
    public void doShutdown() {
        ConnectionPool.getInstance().disconnectAll();
    }

    public ResultSet executeQuery(String sqlQuery) throws SQLException, InterruptedException, StateException {
        if (log.isDebugEnabled()) {
            log.debug("Statement: [" + sqlQuery + "]");
        }

        rs = stmt.executeQuery(sqlQuery);

        return rs;
    }

    public int executeUpdate(String sqlStatement) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("Update-Statement: [" + sqlStatement + "]");
        }

        stmt.executeUpdate(sqlStatement, Statement.RETURN_GENERATED_KEYS);
        rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return -1;
        }
    }
}
