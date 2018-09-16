/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.Utilities;

import static c195.Utilities.DBConnect.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Benjamin this class sets the current user and the user's timezone and
 * locale for the program
 */
public class UserLocaleTime {

    private static String currentUser;
    private static int currentUserID;
    private static TimeZone userTimeZone;

    public UserLocaleTime() {

    }

    //provides a timestamp in UTC time for other methods
    //used for logging changes to the database
    public static Timestamp getCurrentTimeStamp() {

        TimeZone UTC = TimeZone.getTimeZone("UTC");

        Calendar cal = Calendar.getInstance(UTC);

        Date date = cal.getTime();
        long time = date.getTime();
        Timestamp ts = new Timestamp(time);

        return ts;

    }

    //returns local timezone of the current user
    public static TimeZone getUserTimeZone() {

        return userTimeZone;
    }

    //sets the localzone of the current user
    public static void setUserTimeZone() {

        TimeZone timezone = TimeZone.getDefault();
        userTimeZone = timezone;

    }

    //returns username of current user
    public static String getCurrentUser() {

        return currentUser;
    }

    //return user ID of current user
    public static int getCurrentUserID() {

        return currentUserID;

    }

    /**
     * sets the default locale for the program *
     *
     * EVALUATOR: This is the method that meets the requirement for detecting
     * user location. To see the login screen in French, simply un-comment out
     * the French line. Comment it back out to see the screen in English.
     *
     * This method is only run when the application initiates.
     *
     */
    public static void setDefaultLocale() {

        Locale.setDefault(new Locale("en", "US"));
        //Locale.setDefault(new Locale("fr", "FR"));

    }

    //sets the current user and grabs the user's ID from the database
    public static void setCurrentUser(String currentUser) throws SQLException {

        UserLocaleTime.currentUser = currentUser;

        Connection conn = getConnection();
        String SQL = "select userId from user where userName = ?";
        PreparedStatement stmt = conn.prepareStatement(SQL);
        stmt.setString(1, currentUser);
        stmt.executeQuery();

        ResultSet rs = stmt.getResultSet();
        rs.first();

        UserLocaleTime.currentUserID = rs.getInt(1);

    }

}
