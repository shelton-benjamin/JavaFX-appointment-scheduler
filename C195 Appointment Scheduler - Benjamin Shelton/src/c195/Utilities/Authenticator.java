/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.Utilities;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Benjamin
 * class takes username, password, and result set provided by login screen 
 * and determines if given username and password match a user in the database
 */
public class Authenticator {

    public Authenticator() {

    }

    public static boolean authenticate(String username, String password, ResultSet rs) throws SQLException{

        while (rs.next()) {
            
            String correctUsername = rs.getString("userName");
            String correctPassword = rs.getString("password");
            
            if (correctUsername.equals(username) && correctPassword.equals(password)) {
                
                return true;
            }
        }

        return false;
    }

}
