/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.Utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Benjamin
 * this class creates the connection to the database when the application initiates
 * and closes it when the application exits
 */
public class DBConnect {

    private static Connection conn;

    public DBConnect() {
    }

    public static void doConnect() {

        String url = "jdbc:mysql://52.206.157.109/U04Dvq";
        String user = "U04Dvq";
        String pass = "53688208747";

        try {
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connection successful.");

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());

        }

    }
    
    public static Connection getConnection() {
        
        return conn;
    }

    public static void closeConnect() {

        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        } finally {
            System.out.println("Connection terminated.");
        }
    }
}
