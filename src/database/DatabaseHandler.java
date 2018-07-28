package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javax.swing.JOptionPane;
import analytics.Analytics;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;

public final class DatabaseHandler {
    
    private static DatabaseHandler handler = null;
    
    private static final String DB_URL = "jdbc:derby:database; create=true";
    private static Connection conn = null;
    private static Statement stmt = null;
    
    private DatabaseHandler() {
            createConnection();
            setupTable();
            setupCategoryTable();
            setupEmailHistoryTable();
            setupEmailTable();
            setupStatisticsTable();
    }
    
    public static DatabaseHandler getInstance() {
        if (handler == null)
            handler = new DatabaseHandler();
        return handler;
    }
    
    void createConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = DriverManager.getConnection(DB_URL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    void setupTable() {
        String tableName = "CUSTOMERS";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName.toUpperCase(), null);
            if (tables.next()) {
                System.out.println("Table " + tableName + " already exists. Ready to go!");
            } else {
                stmt.execute("CREATE TABLE " + tableName + "("
                        + "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n"
                        + "Name VARCHAR(40),\n"
                        + "Surname VARCHAR(40),\n"
                        + "Email VARCHAR(50),\n"
                        + "Category VARCHAR(150),\n"
                        + "PRIMARY KEY(ID))");
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage() + " ... setup Database");
        }
    }
    
    
    void setupCategoryTable() {
        String tableName = "CATEGORIES";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName.toUpperCase(), null);
            if (tables.next()) {
                System.out.println("Table " + tableName + " already exists. Ready to go!");
            } else {
                stmt.execute("CREATE TABLE " + tableName + "(Category VARCHAR(50) PRIMARY KEY)");
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage() + " ... setup Database");
        }
    }
    
    void setupEmailHistoryTable() {
        String tableName = "HISTORY";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName.toUpperCase(), null);
            if (tables.next()) {
                System.out.println("Table " + tableName + " already exists. Ready to go!");
            } else {
                stmt.execute("CREATE TABLE " + tableName + "("
                        + "ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),\n"
                        + "Recipient VARCHAR(100),\n"
                        + "Subject VARCHAR(100),\n"
                        + "Date timestamp default CURRENT_TIMESTAMP,\n"
                        + "PRIMARY KEY(ID))");
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage() + " ... setup Database");
        }
    }
    
    void setupEmailTable() {
        String tableName = "EMAILLOGIN";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName.toUpperCase(), null);
            if (tables.next()) {
                System.out.println("Table " + tableName + " already exists. Ready to go!");
            } else {
                stmt.execute("CREATE TABLE " + tableName + "("
                        + "Login VARCHAR(50) PRIMARY KEY,\n"
                        + "Password VARCHAR(50))");
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage() + " ... setup Database");
        }
    }
    
    void setupStatisticsTable() {
        String tableName = "STATISTICS";
        try {
            stmt = conn.createStatement();
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, tableName.toUpperCase(), null);
            if (tables.next()) {
                System.out.println("Table " + tableName + " already exists. Ready to go!");
            } else {
                stmt.execute("CREATE TABLE " + tableName + "("
                    + "sent_counter INTEGER default 0, \n"
                    + "sent_opened INTEGER default 0)");
                stmt.executeUpdate("INSERT INTO " + tableName + "(sent_counter, sent_opened) VALUES (0,0)");
            }   
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ResultSet execQuery(String query) {
        ResultSet result;
        try {
            stmt = conn.createStatement();
            result = stmt.executeQuery(query);
        } catch (SQLException ex) {
            System.out.println("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
            return null;
        }
        return result;
    }
    
    public boolean execAction(String query) {
        try {
            stmt = conn.createStatement();
            stmt.execute(query);
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Erro Occured", JOptionPane.ERROR_MESSAGE);
            System.out.println("Exception at execQuery:dataHandler" + ex.getLocalizedMessage());
            return false;
        }
    }
    
    public int execUpdate(String query) throws SQLException {
        stmt = conn.createStatement();
        int result = stmt.executeUpdate(query);
        return result;
    }
        
    public ObservableList<PieChart.Data> getStatistics() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();   
        try {
            ResultSet rs = execQuery("SELECT * FROM STATISTICS");
            while (rs.next()) {
                int sent_counter = rs.getInt("sent_counter");
                if (sent_counter == 0) {
                    stmt.executeUpdate("UPDATE STATISTICS SET sent_opened = " + Analytics.getEventsNumber() + "");
                    data = null;
                    break;
                } else {
                    try {
                        AnalyticsReporting service = Analytics.initializeAnalyticsReporting();
                        GetReportsResponse response = Analytics.getReport(service);
                        Analytics.printResponse(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int sent_opened = Analytics.getEventsNumber() - rs.getInt("sent_opened");
                    if (sent_opened > sent_counter) {
                        sent_opened = sent_counter;
                    }
                    data.add(new PieChart.Data("Total Sent Emails (" + sent_counter + ")", sent_counter));
                    data.add(new PieChart.Data("Total Opened Emails (" + sent_opened + ")", sent_opened));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    
}
