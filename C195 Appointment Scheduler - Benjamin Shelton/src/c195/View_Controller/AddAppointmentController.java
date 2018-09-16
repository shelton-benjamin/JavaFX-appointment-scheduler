/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import static c195.Model.Appointment.appointmentValuesChecker;
import static c195.Model.Appointment.getApptLocations;
import static c195.Model.Appointment.getApptTypes;
import c195.Model.Customer;
import static c195.Utilities.DBConnect.getConnection;
import static c195.Utilities.UserLocaleTime.getCurrentTimeStamp;
import static c195.Utilities.UserLocaleTime.getCurrentUser;
import static c195.Utilities.UserLocaleTime.getCurrentUserID;
import static c195.Utilities.UserLocaleTime.getUserTimeZone;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
public class AddAppointmentController implements Initializable {

    @FXML
    private TableView<Customer> customerTableView;
    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private ComboBox locationCB;
    @FXML
    private ComboBox appointmentTypeCB;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox startTimeCB;
    @FXML
    private ComboBox endTimeCB;
    @FXML
    private TextField descriptionField;

    @FXML
    private TextField searchCustomerField;
    @FXML
    private Button searchCustomerButton;

    @FXML
    private Button saveAppointmentButton;
    @FXML
    private Button cancelButton;

    private ObservableList<Customer> customerList;

    //refreshes customer tableview for this scene when it opens
    //grabs all customers from database
    @FXML
    private void updateCustomerList() {

        try {
            customerList.clear();
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            String SQL = "select * from customer";

            ResultSet rs = stmt.executeQuery(SQL);

            while (rs.next()) {

                int newID = rs.getInt("customerId");
                String newName = rs.getString("customerName");
                int addressID = rs.getInt("addressId");

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
        } catch (SQLException ex) {
            Logger.getLogger(AddAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    //adds appointment to database if information is valid
    @FXML
    private void saveAppointmentClicked() throws SQLException, ParseException {

        //error message text
        String header = "Invalid input.";
        StringBuilder sbuf = new StringBuilder();
        String message = "Select a";
        sbuf.append(message);
        String field = null;

        //gets timestamp and current user
        Timestamp currentTS = getCurrentTimeStamp();
        String currentUser = getCurrentUser();
        int currentUserID = getCurrentUserID();
        TimeZone currentTZ = getUserTimeZone();

        //grabs customer for appointment
        Customer myCustomer = customerTableView.getSelectionModel().getSelectedItem();
        int customerID;

        //grabs date and times selected
        LocalDate appointmentDate = datePicker.getValue();
        String startTime = (String) startTimeCB.getSelectionModel().getSelectedItem();
        String endTime = (String) endTimeCB.getSelectionModel().getSelectedItem();

        //formatters for converting user timezone to UTC
        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inFormat.setTimeZone(currentTZ);
        outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {

            field = " customer.";
            customerID = myCustomer.getCustomerId();

            field = " date.";
            String date = appointmentDate.toString();

            field = " start time.";
            startTime = outFormat.format(inFormat.parse(date.concat(" ").concat(startTime)));

            field = "n end time.";
            endTime = outFormat.format(inFormat.parse(date.concat(" ").concat(endTime)));

        } catch (NullPointerException ex) {

            //if any of the above fields are left blank by user
            //displayes error message and returns method
            sbuf.append(field);
            JOptionPane.showMessageDialog(null, sbuf.toString(), header, 0);
            return;
        }

        int appointmentID = 0; //field autoincrements in database
        int userID = currentUserID;
        String type = (String) appointmentTypeCB.getSelectionModel().getSelectedItem();
        String description = descriptionField.getText();
        String location = (String) locationCB.getSelectionModel().getSelectedItem();
        String contact = "";
        String url = "";
        String start = startTime;
        String end = endTime;
        Timestamp createDate = currentTS;
        String createdBy = currentUser;
        Timestamp lastUpdate = currentTS;
        String lastUpdateBy = currentUser;

        try {

            field = "n appointment type.";
            boolean test = type.isEmpty();

            field = " location.";
            test = location.isEmpty();

        } catch (NullPointerException ex) {

            //if any of the above fields are left blank by user
            //displayes error message and returns method
            sbuf.append(field);
            JOptionPane.showMessageDialog(null, sbuf.toString(), header, 0);
            return;
        }

        //passes appointment information to checker method to ensure info is valid
        boolean validInput = appointmentValuesChecker(appointmentID, customerID, location, start, end);

        //if all input is valid, the new appointment is saved to the database
        if (validInput) {

            Connection conn = getConnection();
            PreparedStatement stmt;

            String appointmentSQL = "insert into appointment values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(appointmentSQL);
            stmt.setInt(1, appointmentID);
            stmt.setInt(2, customerID);
            stmt.setString(3, type);
            stmt.setString(4, description);
            stmt.setString(5, location);
            stmt.setString(6, contact);
            stmt.setString(7, url);
            stmt.setString(8, start);
            stmt.setString(9, end);
            stmt.setTimestamp(10, createDate);
            stmt.setString(11, createdBy);
            stmt.setTimestamp(12, lastUpdate);
            stmt.setString(13, lastUpdateBy);
            stmt.executeUpdate();

            System.out.println("Appointment added.");

            Stage stage = (Stage) saveAppointmentButton.getScene().getWindow();
            stage.close();

        }

    }

    //closes window
    @FXML
    private void cancelButtonClicked() {

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    //fills the location combobox
    private void setLocationList() {

        List<String> locationList = getApptLocations();

        locationCB.getItems().addAll(locationList);
    }

    //fills the type combobox
    private void setTypeList() {

        List<String> appointmentTypes = getApptTypes();

        appointmentTypeCB.getItems().addAll(appointmentTypes);
    }

    //fills the start time combobox
    private void fillStartTime() {

        fillTimeCB(startTimeCB, true);

    }

    //fills the end time combobox
    private void fillEndTime() {

        fillTimeCB(endTimeCB, false);

    }

    //generates the times in proper formats for start and end time comboboxes
    private void fillTimeCB(ComboBox combo, boolean starting) {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        //start time times
        if (starting) {

            start.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, 0, 0);
            end.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, 0, 0);
            end.add(Calendar.DAY_OF_MONTH, 1);

            //end time times
        } else {

            start.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, 0, 30);
            end.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, 0, 30);
            end.add(Calendar.DAY_OF_MONTH, 1);

        }

        List<String> times = new ArrayList();

        while (end.after(start)) {

            Calendar cal = (Calendar) start.clone();
            Date date = new Date();
            date.setTime(cal.getTimeInMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            times.add(dateFormat.format(date));
            start.add(Calendar.MINUTE, 30);
        }

        combo.getItems().addAll(times);

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

        setLocationList();
        setTypeList();
        fillStartTime();
        fillEndTime();
    }

}
