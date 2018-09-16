/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import c195.Model.Appointment;
import static c195.Model.Appointment.appointmentValuesChecker;
import static c195.Model.Appointment.getApptLocations;
import static c195.Model.Appointment.getApptTypes;
import c195.Model.Customer;
import static c195.Utilities.DBConnect.getConnection;
import static c195.Utilities.UserLocaleTime.getCurrentTimeStamp;
import static c195.Utilities.UserLocaleTime.getCurrentUser;
import static c195.Utilities.UserLocaleTime.getUserTimeZone;
import static c195.View_Controller.MainScreenController.getModifyAppointment;
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
public class ModifyAppointmentController implements Initializable {

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
    Appointment myAppointment;
    ResultSet appointmentInfo;

    //grabs info for appointment being edited from database
    @FXML
    private void setAppointmentInfo() {

        try {
            myAppointment = getModifyAppointment();
            int apptID = myAppointment.getAppointmentID();

            Connection conn = getConnection();
            String SQL = "select * from appointment where appointmentId = ?";
            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setInt(1, apptID);
            stmt.executeQuery();
            appointmentInfo = stmt.getResultSet();
            appointmentInfo.first();

        } catch (SQLException ex) {
            Logger.getLogger(ModifyAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

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

                int customerID = myAppointment.getCustomerID();
                Customer myCustomer = new Customer();

                for (Customer c : customerList) {

                    int tempID = c.getCustomerId();
                    if (tempID == customerID) {

                        myCustomer = c;
                    }

                }
                customerTableView.getSelectionModel().select(myCustomer);

            } else {

                System.out.println("Customer list empty.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ModifyAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
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

    //saves edited appointment info to database if all input is valid
    @FXML
    private void saveAppointmentClicked() throws ParseException, SQLException {

        String message = "Invalid input.";
        Timestamp currentTS = getCurrentTimeStamp();
        String currentUser = getCurrentUser();
        TimeZone currentTZ = getUserTimeZone();

        Customer myCustomer = customerTableView.getSelectionModel().getSelectedItem();
        int customerID;

        LocalDate appointmentDate = datePicker.getValue();
        String startTime = (String) startTimeCB.getSelectionModel().getSelectedItem();
        String endTime = (String) endTimeCB.getSelectionModel().getSelectedItem();

        SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inFormat.setTimeZone(currentTZ);
        outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String date;

        try {
            date = appointmentDate.toString();

        } catch (NullPointerException ex) {

            JOptionPane.showMessageDialog(null, "Select an appointment date.", message, 0);
            return;
        }

        startTime = outFormat.format(inFormat.parse(date.concat(" ").concat(startTime)));
        endTime = outFormat.format(inFormat.parse(date.concat(" ").concat(endTime)));

        try {
            customerID = myCustomer.getCustomerId();
        } catch (NullPointerException ex) {

            JOptionPane.showMessageDialog(null, "Select a customer.", message, 0);
            return;
        }

        int appointmentID = myAppointment.getAppointmentID();
        String type = (String) appointmentTypeCB.getSelectionModel().getSelectedItem();
        String description = descriptionField.getText();
        String location = (String) locationCB.getSelectionModel().getSelectedItem();
        String start = startTime;
        String end = endTime;
        Timestamp lastUpdate = currentTS;
        String lastUpdateBy = currentUser;

        boolean validInput = appointmentValuesChecker(appointmentID, customerID, location, start, end);

        if (validInput) {

            Connection conn = getConnection();

            String SQL = "update appointment set customerId = ?, title = ?, "
                    + "description = ?, location = ?, start = ?, end = ?,"
                    + "lastUpdate = ?, lastUpdateBy = ? where appointmentId = ?";

            PreparedStatement stmt = conn.prepareStatement(SQL);
            stmt.setInt(1, customerID);
            stmt.setString(2, type);
            stmt.setString(3, description);
            stmt.setString(4, location);
            stmt.setString(5, start);
            stmt.setString(6, end);
            stmt.setTimestamp(7, lastUpdate);
            stmt.setString(8, lastUpdateBy);
            stmt.setInt(9, appointmentID);
            stmt.executeUpdate();

            System.out.println("Appointment updated.");

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

    //fills location combobox and selects saved location of appointment
    private void setLocationList() throws SQLException {

        List<String> locationList = getApptLocations();

        locationCB.getItems().addAll(locationList);

        String location = myAppointment.getLocation();
        locationCB.getSelectionModel().select(location);
    }

    //fills type combobox and selects saved type of appointment
    private void setTypeList() throws SQLException {

        List<String> appointmentTypes = getApptTypes();

        appointmentTypeCB.getItems().addAll(appointmentTypes);

        String type = myAppointment.getAppointmentType();
        appointmentTypeCB.getSelectionModel().select(type);
    }

    //fills start time combobox and selects saved start time of appointment
    private void fillStartTime() throws SQLException {

        fillTimeCB(startTimeCB, true);

        String startTime = myAppointment.getStartTime();
        startTimeCB.getSelectionModel().select(startTime);

    }

    //fills end time combobox and selects saved end time of appointment
    private void fillEndTime() {

        fillTimeCB(endTimeCB, false);

        String endTime = myAppointment.getEndTime();
        endTimeCB.getSelectionModel().select(endTime);

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

    //sets text in description field if available
    private void setDescriptionText() throws SQLException {

        String description = myAppointment.getDescription();
        descriptionField.setText(description);
    }

    //sets saved appointment date in date picker
    private void setDate() throws ParseException {

        SimpleDateFormat inFormat = new SimpleDateFormat("MM-dd-yyyy");
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");

        String date = myAppointment.getDate();
        CharSequence newDate = outFormat.format(inFormat.parse(date));

        datePicker.setValue(LocalDate.parse(newDate));
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            //lambda is used here as it's more efficient when cycling through all
            //customers and adding their names to the tableview
            customerNameColumn.setCellValueFactory(cellData -> cellData.getValue().CustomerNameProperty());
            customerList = FXCollections.observableArrayList();
            setAppointmentInfo();
            updateCustomerList();

            setLocationList();
            setTypeList();
            fillStartTime();
            fillEndTime();
            setDescriptionText();
            setDate();

        } catch (SQLException ex) {
            Logger.getLogger(ModifyAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ModifyAppointmentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
