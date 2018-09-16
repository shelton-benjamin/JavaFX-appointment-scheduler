/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import c195.Model.Appointment;
import static c195.Model.Appointment.dateComparator;
import static c195.Model.Appointment.getApptLocations;
import static c195.Model.Appointment.getApptTypes;
import c195.Model.Customer;
import static c195.Utilities.DBConnect.getConnection;
import static c195.Utilities.UserLocaleTime.getUserTimeZone;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author Benjamin
 */
public class MainScreenController implements Initializable {

    @FXML
    private Button addCustomerButton;
    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private Button deleteCustomer;
    @FXML
    private Button updateCustomer;
    @FXML
    private Button modifyCustomer;

    @FXML
    private Button addAppointmentButton;
    @FXML
    private TableView<Appointment> appointmentTableView;
    @FXML
    private TableColumn<Appointment, String> appointmentCNameColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentLocationColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentTypeColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentDateColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentStartTimeColumn;
    @FXML
    private TableColumn<Appointment, String> appointmentEndTimeColumn;

    @FXML
    private RadioButton defaultCalendarToggle;
    @FXML
    private Button weeklyCalendarToggle;
    @FXML
    private Button monthlyCalendarToggle;

    @FXML
    private Button updateAppointment;
    @FXML
    private Button modifyAppointment;
    @FXML
    private Button deleteAppointment;

    @FXML
    private TextField searchCustomerField;
    @FXML
    private Button searchCustomerButton;

    @FXML
    private Button typeReportButton;
    @FXML
    private Button scheduleReportButton;
    @FXML
    private Button locationReportButton;

    private static ObservableList<Customer> customerList;
    private static Customer customerToModify;

    private static ObservableList<Appointment> appointmentList;
    private static Appointment appointmentToModify;

    private static String generatedReport;

    //launches the screen to add a customer
    @FXML
    private void addCustomerClicked() throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("Add Customer.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Add Customer");
        stage.showAndWait();
        updateCustomerList();
        updateAppointmentList();
    }

    //launches to screen to edit or view an existing customer's info
    @FXML
    private void modifyCustomerClicked() throws Exception {

        try {
            Customer customer = customerTableView.getSelectionModel().getSelectedItem();

            setModifyCustomer(customer);

            System.out.println(customer.getCustomerName());

            Parent root = FXMLLoader.load(getClass().getResource("Modify Customer.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Modify Customer");
            stage.showAndWait();
            updateCustomerList();
            updateAppointmentList();

        } catch (NullPointerException ex) {
            //if no customer selected, do nothing
        }

    }

    //deletes the selected customer from database
    //also deletes associated appointments
    @FXML
    private void deleteCustomerClicked() throws Exception {

        try {

            Customer customerToDelete = customerTableView.getSelectionModel().getSelectedItem();
            int idToDelete = customerToDelete.getCustomerId();

            String message = "Are you sure you want to delete this customer? \n"
                    + "Deleting a customer will also delete their appointments.";

            //confirmation dialog before deletion
            int delete = JOptionPane.showConfirmDialog(null, message,
                    "Delete customer", JOptionPane.YES_NO_OPTION);

            if (delete == JOptionPane.YES_OPTION) {

                String SQL = "delete from customer where customerId = ?";

                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(SQL);
                stmt.setInt(1, idToDelete);
                stmt.executeUpdate();
                updateCustomerList();

                //associated appointments also deleted
                deleteCustAppts(idToDelete);
                updateAppointmentList();

                System.out.println("Customer deleted.");
            }
        } catch (NullPointerException ex) {
            //if no customer selected, do nothing
        }

    }

    //refreshes the customer tableview by grabbing customer info from database
    @FXML
    private void updateCustomerList() {

        try {
            customerList.clear();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            String SQL = "select * from customer";

            ResultSet rs = stmt.executeQuery(SQL);

            //cycles through all customers in database
            while (rs.next()) {

                int newID = rs.getInt("customerId");
                String newName = rs.getString("customerName");
                int addressID = rs.getInt("addressId");

                //creates a new customer instance and adds it to local list
                Customer newCustomer = new Customer();

                newCustomer.setCustomerId(newID);
                newCustomer.setCustomerName(newName);
                newCustomer.setAddressID(addressID);
                customerList.add(newCustomer);
            }

            if (!customerList.isEmpty()) {

                customerTableView.setItems(customerList);

            } else {

                System.out.println("Customer list empty.");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    //sets the customer whose data will be edited
    private void setModifyCustomer(Customer customer) {

        customerToModify = customer;
    }

    //returns the customer whose data will be edited
    public static Customer getModifyCustomer() {

        return customerToModify;
    }

    //searches for customers by name in local list and 
    //updates tableview with search results
    @FXML
    private void searchCustomerClicked() {

        String searchTerm = searchCustomerField.getText();
        ObservableList<Customer> searchList = FXCollections.observableArrayList();

        for (Customer c : customerList) {

            String name = c.getCustomerName().toUpperCase().trim();
            searchTerm = searchTerm.toUpperCase().trim();

            if (name.contains(searchTerm)) {
                searchList.add(c);
            }
        }

        customerTableView.setItems(searchList);
    }

    //launches the screen to add a customer
    @FXML
    private void addAppointmentClicked() throws Exception {

        //prevents screen from opening if no customers are in database
        if (customerList.isEmpty()) {

            JOptionPane.showMessageDialog(null, "Add a customer before adding an appointment.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            Parent root = FXMLLoader.load(getClass().getResource("Add Appointment.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Add Appointment");
            stage.showAndWait();
            updateAppointmentList();
        }
    }

    //launches to screen to edit or view an existing appointments's info
    @FXML
    private void modifyAppointmentClicked() throws IOException, ParseException {

        try {

            Appointment appointment = appointmentTableView.getSelectionModel().getSelectedItem();

            int appointmentID = appointment.getAppointmentID();

            //checks that user isn't trying to modify a date header rather 
            //than a proper appointment
            if (appointmentID > -1) {
                setModifyAppointment(appointment);

                Parent root = FXMLLoader.load(getClass().getResource("Modify Appointment.fxml"));
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modify Appointment");
                stage.showAndWait();
                updateAppointmentList();
            }

        } catch (NullPointerException ex) {
            //if no appointment selected, do nothing
        }
    }

    //deletes the selected appointment from the database
    @FXML
    private void deleteAppointmentClicked() throws SQLException, ParseException {

        try {
            Appointment appointmentToDelete = appointmentTableView.getSelectionModel().getSelectedItem();

            int idToDelete = appointmentToDelete.getAppointmentID();

            //checks that user isn't trying to modify a date header rather 
            //than a proper appointment
            if (idToDelete > -1) {

                String message = "Are you sure you want to delete this appointment?";

                int delete = JOptionPane.showConfirmDialog(null, message,
                        "Delete appointment", JOptionPane.YES_NO_OPTION);

                if (delete == JOptionPane.YES_OPTION) {

                    String SQL = "delete from appointment where appointmentId = ?";

                    Connection conn = getConnection();
                    PreparedStatement stmt = conn.prepareStatement(SQL);
                    stmt.setInt(1, idToDelete);
                    stmt.executeUpdate();

                    updateAppointmentList();

                    System.out.println("Appointment deleted.");
                }
            }

        } catch (NullPointerException ex) {
            //if no appointment selected, do nothing
        }
    }

    //refreshes the appointment tableview by grabbing appointment info from database
    @FXML
    private void updateAppointmentList() throws ParseException {

        try {

            appointmentList.clear();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            String SQL = "select * from appointment";

            ResultSet rs = stmt.executeQuery(SQL);

            //cycles through all appointments in database
            while (rs.next()) {

                int appointmentID = rs.getInt("appointmentId");
                int customerID = rs.getInt("customerId");
                String customerName = getCNamefromDB(customerID);
                String type = rs.getString("title");
                String description = rs.getString("description");
                String location = rs.getString("location");
                String date = rs.getString("start");
                String startTime = rs.getString("start");
                String endTime = rs.getString("end");

                date = date.substring(0, 10);

                SimpleDateFormat inDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat outDateFormat = new SimpleDateFormat("MM-dd-yyyy");

                SimpleDateFormat inTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outTimeFormat = new SimpleDateFormat("hh:mm a");

                inTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                outTimeFormat.setTimeZone(getUserTimeZone());

                date = outDateFormat.format(inDateFormat.parse(date));
                startTime = outTimeFormat.format(inTimeFormat.parse(startTime));
                endTime = outTimeFormat.format(inTimeFormat.parse(endTime));

                //creates a new instance of each appointment in database
                //saves appointment to local list
                Appointment newAppointment = new Appointment();
                newAppointment.setAppointmentID(appointmentID);
                newAppointment.setCustomerID(customerID);
                newAppointment.setCustomerName(customerName);
                newAppointment.setType(type);
                newAppointment.setLocation(location);
                newAppointment.setDescription(description);
                newAppointment.setDate(date);
                newAppointment.setStartTime(startTime);
                newAppointment.setEndTime(endTime);
                newAppointment.setAppointmentDate(date);

                appointmentList.add(newAppointment);

            }

            if (!appointmentList.isEmpty()) {

                appointmentTableView.setItems(appointmentList);

            } else {

                System.out.println("Appointment list is empty.");

            }

        } catch (SQLException ex) {
            Logger.getLogger(MainScreenController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    //sets the appointment whose data will be edited
    private void setModifyAppointment(Appointment appointment) {

        appointmentToModify = appointment;
    }

    //returns the appointment whose data will be edited
    public static Appointment getModifyAppointment() {

        return appointmentToModify;
    }

    //returns the customer name from the database for the corresponding appointment
    private String getCNamefromDB(int customerID) throws SQLException {

        Connection conn = getConnection();
        String SQL = "select customerName from customer where customerId = ?";
        PreparedStatement stmt = conn.prepareStatement(SQL);
        stmt.setInt(1, customerID);
        stmt.executeQuery();
        ResultSet rs = stmt.getResultSet();
        rs.first();
        return rs.getString(1);

    }

    //deletes all associated appointments when a customer is deleted
    private void deleteCustAppts(int customerID) throws SQLException {

        Connection conn = getConnection();
        String SQL = "delete from appointment where customerId = ?";
        PreparedStatement stmt = conn.prepareStatement(SQL);
        stmt.setInt(1, customerID);
        stmt.executeUpdate();

    }

    //sorts appointments by week when clicked
    @FXML
    private void weeklySelected() throws ParseException {

        updateAppointmentList();

        if (!appointmentList.isEmpty()) {
            ArrayList<Appointment> sortedList = new ArrayList<>();
            sortedList.addAll(appointmentList);
            sortedList = appointmentSorter(sortedList, 0, 'w', false);
            appointmentList.clear();
            appointmentList.addAll(sortedList);
            appointmentTableView.setItems(appointmentList);
        }
    }

    //sorts appointments by month when clicked
    @FXML
    private void monthlySelected() throws ParseException {

        updateAppointmentList();

        if (!appointmentList.isEmpty()) {
            ArrayList<Appointment> sortedList = new ArrayList<>();
            sortedList.addAll(appointmentList);
            sortedList = appointmentSorter(sortedList, 0, 'm', false);
            appointmentList.clear();
            appointmentList.addAll(sortedList);
            appointmentTableView.setItems(appointmentList);
        }
    }

    //sorts appointments chronologically and adds appropriate date headers
    //note that the date headers are special instances of appointment class
    private ArrayList appointmentSorter(ArrayList<Appointment> list, int index, char period, boolean sorted) {

        //sorts appointments chronologically and recursively returns to have headers added
        if (!sorted) {

            list.sort(dateComparator);

            return appointmentSorter(list, 0, period, true);

            //adds appropriate header if only one appointment is in database
        } else if (list.size() == 1) {

            //gets date/time info for appointment
            Appointment temp = list.get(index);
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(temp.getAppointmentDate().getTime());

            switch (period) {

                //inserts month headers
                case 'm':

                    String periodText = String.format("%tB %tY", date, date);
                    Appointment blankAppt = new Appointment();
                    blankAppt.setAppointmentID(-1);
                    blankAppt.setCustomerName(periodText);
                    list.add(index, blankAppt);
                    break;

                //inserts week headers
                case 'w':

                    int weekOfYear = date.get(Calendar.WEEK_OF_YEAR);
                    periodText = String.format("Week %d of %tY", weekOfYear, date);
                    blankAppt = new Appointment();
                    blankAppt.setAppointmentID(-1);
                    blankAppt.setCustomerName(periodText);
                    list.add(index, blankAppt);
                    break;
            }

            return appointmentSorter(list, ++index, period, true);

            //returns the sorted list with headers included if the index has reached 
            //the last appointment
        } else if (index == (list.size() - 1)) {

            return list;

        } else {

            //grabs current appointment and next in list
            Appointment temp = list.get(index);
            Appointment next = list.get(index + 1);

            Calendar date1 = Calendar.getInstance();
            Calendar date2 = Calendar.getInstance();

            date1.setTimeInMillis(temp.getAppointmentDate().getTime());
            date2.setTimeInMillis(next.getAppointmentDate().getTime());

            int compare1 = 0;
            int compare2 = 0;

            String periodText = "";

            switch (period) {

                //adds month headers
                case 'm':
                    compare1 = date1.get(Calendar.MONTH);
                    compare2 = date2.get(Calendar.MONTH);

                    //if index is first in list, adds the appropriate header to beginning of list
                    //and recursively returns with next appointment in list
                    if (index == 0) {

                        periodText = String.format("%tB %tY", date1, date1);
                        Appointment blankAppt = new Appointment();
                        blankAppt.setAppointmentID(-1);
                        blankAppt.setCustomerName(periodText);
                        list.add(index, blankAppt);
                        return appointmentSorter(list, ++index, period, true);
                    }

                    periodText = String.format("%tB %tY", date2, date2);

                    break;

                //adds week headers
                case 'w':
                    compare1 = date1.get(Calendar.WEEK_OF_YEAR);
                    compare2 = date2.get(Calendar.WEEK_OF_YEAR);

                    //if index is first in list, adds the appropriate header to beginning of list
                    //and recursively returns with next appointment in list
                    if (index == 0) {

                        periodText = String.format("Week %d of %tY", compare1, date1);
                        Appointment blankAppt = new Appointment();
                        blankAppt.setAppointmentID(-1);
                        blankAppt.setCustomerName(periodText);
                        list.add(index, blankAppt);
                        return appointmentSorter(list, ++index, period, true);
                    }

                    periodText = String.format("Week %d of %tY", compare2, date2);
                    break;
            }

            //cycles to next appointment if next appointment is of same time period
            //as current appointment
            if (compare1 == compare2) {

                return appointmentSorter(list, ++index, period, true);

                //if next appointment not from same time period
                //an appropriate header is added to the list after current appointment
                //and index cycles to appointment after the added header
            } else {

                Appointment blankAppt = new Appointment();
                blankAppt.setAppointmentID(-1);
                blankAppt.setCustomerName(periodText);
                list.add(++index, blankAppt);
                return appointmentSorter(list, ++index, period, true);
            }

        }

    }

    //checks if there is an appointment within 15 minutes of sign in
    private void imminentAppt() throws ParseException {

        boolean soon = false;

        Calendar cal1 = Calendar.getInstance();
        long currentTime = cal1.getTimeInMillis();
        cal1.add(Calendar.MINUTE, 15);
        long fifteenMin = cal1.getTimeInMillis();

        String message = "The following appointment(s) is/are scheduled within "
                + "the next 15 minutes:";
        for (Appointment a : appointmentList) {

            String date = a.getDate();
            String start = a.getStartTime();
            String combined = date.concat(" ").concat(start);

            SimpleDateFormat inFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm a");

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(inFormat.parse(combined));

            long appointmentTime = cal2.getTimeInMillis();

            if (appointmentTime > currentTime && appointmentTime < fifteenMin) {

                String customer = a.getCustomerName();
                date = a.getDate();
                String location = a.getLocation();
                start = a.getStartTime();
                String end = a.getEndTime();

                StringBuilder appt = new StringBuilder();
                Formatter fmt = new Formatter(appt);
                fmt.format("\n %s %s %s %s -- %s", customer, date, location, start, end);
                message = message.concat(appt.toString());
                soon = true;
            }
        }

        //if there is at least one imminent appointment
        //provides list of appointments
        if (soon) {

            JOptionPane.showMessageDialog(null, message, "Upcoming appointments",
                    JOptionPane.INFORMATION_MESSAGE);

            //tells user there are no appointment within 15 minutes
            //if there are none
        } else {

            JOptionPane.showMessageDialog(null, "You have no appointments within "
                    + "the next 15 minutes.", "Upcoming appointments",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //method that opens report window
    //and takes the report body and report title as parameters
    private void generateReport(String report, String title) {

        try {
            generatedReport = report;
            Parent root = FXMLLoader.load(getClass().getResource("Report.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(MainScreenController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    //returns the report saved in main screen
    //used by Report Controller
    public static String getReport() {

        return generatedReport;
    }

    //this report shows the number of appointment types in each month
    //with at least one scheduled appointment
    @FXML
    private void typeReportButtonClicked() throws ParseException {

        updateAppointmentList();

        String title = "Appointment types by month";
        StringBuilder report = new StringBuilder();
        Formatter fmt = new Formatter(report);

        if (appointmentList.isEmpty()) {

            report.append("There are no scheduled appointments.");
            generateReport(report.toString(), title);

        } else {

            ArrayList<String> types = (ArrayList<String>) getApptTypes();
            ArrayList<Appointment> appointments = new ArrayList<>();
            appointments.addAll(appointmentList);
            appointments = appointmentSorter(appointments, 0, 'm', false);

            int[] count = new int[types.size()];
            int countIndex = 0;

            boolean first = true;

            for (Appointment appt : appointments) {

                countIndex = 0;

                if (appt.getAppointmentID() == -1) {

                    if (!first) {

                        countIndex = 0;
                        for (String type : types) {
                            fmt.format("%s: %d\n", type, count[countIndex]);
                            countIndex++;
                        }
                        report.append("\n");

                        for (int index = 0; count.length > index; index++) {

                            count[index] = 0;
                        }
                    }

                    report.append(appt.getCustomerName()).append("\n");
                    first = false;

                } else {

                    for (String type : types) {

                        if (type.equals(appt.getAppointmentType())) {

                            count[countIndex]++;
                        }
                        countIndex++;
                    }
                }

            }

            countIndex = 0;
            for (String type : types) {
                fmt.format("%s: %d\n", type, count[countIndex]);
                countIndex++;
            }
            generateReport(report.toString(), title);
        }
    }

    //this report shows the schedule for each consultant in customer list
    //also provides message if consultatn has no associated appointments
    @FXML
    private void scheduleReportButtonClicked() throws ParseException {

        updateCustomerList();
        updateAppointmentList();

        StringBuilder report = new StringBuilder();
        Formatter fmt = new Formatter(report);
        String title = "Consultant schedules";

        if (customerList.isEmpty()) {

            report.append("There are no customers in the database.");
            generateReport(report.toString(), title);
        } else {

            ArrayList<Appointment> appointments = new ArrayList<>();
            appointments.addAll(appointmentList);
            appointments = appointmentSorter(appointments, 0, 'w', false);

            for (Customer cust : customerList) {

                String custName = cust.getCustomerName();
                fmt.format("%s\n", custName);
                boolean none = true;

                for (Appointment appt : appointments) {

                    String apptName = appt.getCustomerName();

                    if (custName.equals(apptName)) {

                        none = false;
                        String location = appt.getLocation();
                        String date = appt.getDate();
                        String start = appt.getStartTime();
                        String end = appt.getEndTime();

                        fmt.format("%s %s %s –- %s\n", date, location, start, end);
                    }
                }
                if (none) {

                    report.append("Consultant has no scheduled appointments.\n");
                }
                report.append("\n");
            }

            generateReport(report.toString(), title);
        }
    }

    //this report provides the number of appointments scheduled for each location
    //assuming there is at least one scheduled appointment anywhere
    @FXML
    private void locationReportButtonClicked() throws ParseException {

        updateAppointmentList();

        StringBuilder report = new StringBuilder();
        Formatter fmt = new Formatter(report);
        String title = "Appointments per location";

        if (appointmentList.isEmpty()) {

            report.append("There are no scheduled appointments.");
            generateReport(report.toString(), title);

        } else {

            ArrayList<String> locations = (ArrayList<String>) getApptLocations();

            ArrayList<Appointment> appointments = new ArrayList<>();
            appointments.addAll(appointmentList);

            int count = 0;

            for (String location : locations) {

                for (Appointment appt : appointments) {

                    String apptLocation = appt.getLocation();

                    if (location.equals(apptLocation)) {

                        count++;
                    }
                }

                fmt.format("%s: %d\n", location, count);
                count = 0;
            }

            generateReport(report.toString(), title);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //lambda is used here as it's more efficient when cycling through all
        //customers and adding their names to the tableview
        customerNameColumn.setCellValueFactory(cellData -> cellData.getValue().CustomerNameProperty());
        customerList = FXCollections.observableArrayList();
        updateCustomerList();

        //lambda is used here as it's more efficient when cycling through all
        //appointments and adding their information to the tableview
        appointmentCNameColumn.setCellValueFactory(cellData -> cellData.getValue().CustomerNameProperty());
        appointmentLocationColumn.setCellValueFactory(cellData -> cellData.getValue().LocationProperty());
        appointmentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().TypeProperty());
        appointmentDateColumn.setCellValueFactory(cellData -> cellData.getValue().DateProperty());
        appointmentStartTimeColumn.setCellValueFactory(cellData -> cellData.getValue().StartTimeProperty());
        appointmentEndTimeColumn.setCellValueFactory(cellData -> cellData.getValue().EndTimeProprety());
        appointmentList = FXCollections.observableArrayList();

        try {
            updateAppointmentList();
            imminentAppt();

        } catch (ParseException ex) {
            Logger.getLogger(MainScreenController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

}
