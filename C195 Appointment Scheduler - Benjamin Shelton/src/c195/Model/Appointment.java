/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.Model;

import static c195.Utilities.DBConnect.getConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.swing.JOptionPane;

/**
 *
 * @author Benjamin
 * this class is used to fill in the tableview on the main screen
 * data is pulled from the MySQL database
 */
public class Appointment {

    private IntegerProperty appointmentID;
    private int customerID;
    private StringProperty customerName;
    private StringProperty location;
    private StringProperty type;
    private StringProperty date;
    private StringProperty startTime;
    private StringProperty endTime;
    private String description;
    private Date appointmentDate;

    public Appointment() {

        super();
        appointmentID = new SimpleIntegerProperty();
        customerName = new SimpleStringProperty();
        location = new SimpleStringProperty();
        type = new SimpleStringProperty();
        date = new SimpleStringProperty();
        startTime = new SimpleStringProperty();
        endTime = new SimpleStringProperty();

    }

    
    //getters and setters
    public IntegerProperty AppointmentIDProperty() {
        return appointmentID;
    }

    public void setAppointmentID(int appointmentID) {
        this.appointmentID.set(appointmentID);
    }

    public int getAppointmentID() {

        return appointmentID.get();
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public StringProperty CustomerNameProperty() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }

    public String getCustomerName() {

        return customerName.get();
    }

    public StringProperty LocationProperty() {
        return location;
    }

    public void setLocation(String location) {
        this.location.set(location);
    }

    public String getLocation() {

        return location.get();
    }

    public StringProperty TypeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getAppointmentType() {

        return type.get();
    }

    public StringProperty DateProperty() {
        return date;
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public String getDate() {

        return date.get();
    }

    public StringProperty StartTimeProperty() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime.set(startTime);
    }

    public String getStartTime() {

        return startTime.get();
    }

    public StringProperty EndTimeProprety() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime.set(endTime);
    }

    public String getEndTime() {

        return endTime.get();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getAppointmentDate() {

        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {

        this.appointmentDate = appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date newDate = dateFormat.parse(appointmentDate);
        this.appointmentDate = newDate;
    }

    public static List<String> getApptTypes() {

        List<String> apptTypes = new ArrayList<>();
        apptTypes.add("New Client");
        apptTypes.add("Project Proposal");
        apptTypes.add("Design");
        apptTypes.add("Evaluation");
        apptTypes.add("Negotiation");
        apptTypes.add("Legal");
        apptTypes.add("Closing");
        apptTypes.add("Other");

        return apptTypes;

    }

    public static List<String> getApptLocations() {

        List<String> apptLocations = new ArrayList<>();
        apptLocations.add("Phoenix");
        apptLocations.add("New York");
        apptLocations.add("London");

        return apptLocations;
    }

    
    //sorts appointments by date in descending order
    public static Comparator<Appointment> dateComparator = new Comparator<Appointment>() {

        @Override
        public int compare(Appointment appointment1, Appointment appointment2) {

            long date1 = appointment1.getAppointmentDate().getTime();
            long date2 = appointment2.getAppointmentDate().getTime();

            if (date1 < date2) {

                return -1;
            } else if (date2 < date1) {

                return 1;
            } else {

                return 0;
            }
        }
    };

    
    //ensures each appointment has valid data, does not overlap with other appointments
//    and scheduled inside business hours
    public static boolean appointmentValuesChecker(int appointmentID, int customerID, String location, String startTime, String endTime) {

        String header = "Invalid input.";

        //date/time going into appointment object is converted to UTC
        TimeZone UTC = TimeZone.getTimeZone("UTC");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(UTC);

        Calendar compare1 = Calendar.getInstance(UTC);
        Calendar compare2 = Calendar.getInstance(UTC);

        try {
            compare1.setTime(format.parse(startTime));
            compare2.setTime(format.parse(endTime));
        } catch (ParseException ex) {
            Logger.getLogger(Appointment.class.getName()).log(Level.SEVERE, null, ex);
        }

        //checks to ensure user selected an appointment start time before the end time and vice versa
        if (!(compare1.before(compare2))) {

            JOptionPane.showMessageDialog(null, "Start time must be before end time.", header, 0);
            return false;
        }

        TimeZone locationTZ = TimeZone.getDefault();

        Calendar localTime1 = Calendar.getInstance();
        Calendar localTime2 = Calendar.getInstance();

        //sets variable locationTZ according to local timezone of appointment location
        switch (location) {

            case "Phoenix":
                locationTZ = TimeZone.getTimeZone("America/Phoenix");
                break;

            case "New York":
                locationTZ = TimeZone.getTimeZone("America/New_York");
                break;

            case "London":
                locationTZ = TimeZone.getTimeZone("Europe/London");
                break;
        }

        localTime1 = (Calendar) compare1.clone();
        localTime2 = (Calendar) compare2.clone();

        localTime1.setTimeZone(locationTZ);
        localTime2.setTimeZone(locationTZ);

        Calendar boundary1 = Calendar.getInstance(locationTZ);
        Calendar boundary2 = Calendar.getInstance(locationTZ);

        long time1 = localTime1.getTimeInMillis();
        long time2 = localTime2.getTimeInMillis();

        boundary1.setTimeInMillis(time1);
        boundary2.setTimeInMillis(time2);

        boundary1.set(Calendar.HOUR_OF_DAY, 8);
        boundary1.set(Calendar.MINUTE, 0);

        boundary2.set(Calendar.HOUR_OF_DAY, 17);
        boundary2.set(Calendar.MINUTE, 0);

        int saturday = Calendar.SATURDAY;
        int sunday = Calendar.SUNDAY;

        int apptDay = localTime1.get(Calendar.DAY_OF_WEEK);

        //makes sure that appointment is between 8 am and 5 pm local location time
//        and is not on a Saturday or Sunday
        if (localTime1.before(boundary1) || localTime2.after(boundary2)
                || apptDay == saturday || apptDay == sunday) {

            String businessHours = "Appointment must be scheduled during "
                    + "business hours. Business hours are 8 am - 5 pm "
                    + "(branch's local time) Monday through Friday.";

            JOptionPane.showMessageDialog(null, businessHours, header, 0);
            return false;

        }

        //if above checks are met, method grabs all appointments at the same location from database
        try {

            Connection conn = getConnection();
            String SQL = "select * from appointment where location = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setString(1, location);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();

            boolean overlap = false;

            //cycles through every appointment for that location
            while (rs.next()) {

                Calendar prevApptStart = Calendar.getInstance();
                Calendar prevApptEnd = Calendar.getInstance();

                String timeString1 = rs.getString("start");
                String timeString2 = rs.getString("end");

                try {
                    prevApptStart.setTime(format.parse(timeString1));
                    prevApptEnd.setTime(format.parse(timeString2));

                } catch (ParseException ex) {
                    Logger.getLogger(Appointment.class.getName()).log(Level.SEVERE, null, ex);
                }

                //ensures no overlap between new appointment and existing appointments
                if ((compare1.after(prevApptStart) & compare1.before(prevApptEnd))
                        || (compare2.after(prevApptStart) & compare2.before(prevApptEnd))
                        || compare1.equals(prevApptStart) || compare2.equals(prevApptEnd)) {

                    int prevApptID = rs.getInt("appointmentId");

                    //ensures that modifying an appointment won't throw an overlap error 
                    //because previously saved appointment was being compared with itself
                    if (!(appointmentID == prevApptID)) {

                        overlap = true;
                    }
                }

            }
            if (overlap) {

                JOptionPane.showMessageDialog(null, "This appointment would conflict "
                        + "with another appointment already scheudled at this location.",
                        header, 0);
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(Appointment.class.getName()).log(Level.SEVERE, null, ex);
        }

        //if all checks are met, returns true
        return true;
    }
}
