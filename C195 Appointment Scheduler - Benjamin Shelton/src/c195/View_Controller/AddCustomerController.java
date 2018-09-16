/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import static c195.Model.Customer.customerValuesChecker;
import static c195.Utilities.DBConnect.getConnection;
import static c195.Utilities.UserLocaleTime.getCurrentTimeStamp;
import static c195.Utilities.UserLocaleTime.getCurrentUser;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Benjamin
 */
public class AddCustomerController implements Initializable {

    @FXML
    private TextField addCustomerName;
    @FXML
    private TextField addCustomerAddressLine1;
    @FXML
    private TextField addCustomerAddressLine2;
    @FXML
    private TextField addCustomerCity;
    @FXML
    private TextField addCustomerPostalCode;
    @FXML
    private TextField addCustomerCountry;
    @FXML
    private TextField addCustomerPhone;
    @FXML
    private Button addCustomerSave;
    @FXML
    private Button cancelButton;

    //adds customer to database if information is valid
    @FXML
    private void addSaveButtonClicked() {

        String name = addCustomerName.getText();
        String address1 = addCustomerAddressLine1.getText();
        String address2 = addCustomerAddressLine2.getText();
        String city = addCustomerCity.getText();
        String postalCode = addCustomerPostalCode.getText();
        String country = addCustomerCountry.getText();
        String phone = addCustomerPhone.getText();

        //passes customer information to checker method to ensure info is valid
        boolean validInput = customerValuesChecker(name, address1, city,
                postalCode, country, phone);
        
        //if all input is valid, the new appointment is saved to the database
        if (validInput) {
            try {

                int newID = 0; //ID fields are autoincrement in database

                int active = 1;

                Connection conn = getConnection();
                PreparedStatement stmt;
                ResultSet rs;

                Timestamp currentTS = getCurrentTimeStamp();
                String currentUser = getCurrentUser();

                //adding a country
                String countrySQL = "insert into country values (?,?,?,?,?,?)";
                stmt = conn.prepareStatement(countrySQL, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, newID);
                stmt.setString(2, country);
                stmt.setTimestamp(3, currentTS);
                stmt.setString(4, currentUser);
                stmt.setTimestamp(5, currentTS);
                stmt.setString(6, currentUser);

                stmt.executeUpdate();

                rs = stmt.getGeneratedKeys();
                rs.first();
                int countryID = rs.getInt(1);

                //adding a city
                String citySQL = "insert into city values (?,?,?,?,?,?,?)";
                stmt = conn.prepareStatement(citySQL, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, newID);
                stmt.setString(2, city);
                stmt.setInt(3, countryID);
                stmt.setTimestamp(4, currentTS);
                stmt.setString(5, currentUser);
                stmt.setTimestamp(6, currentTS);
                stmt.setString(7, currentUser);

                stmt.executeUpdate();

                rs = stmt.getGeneratedKeys();
                rs.first();
                int cityID = rs.getInt(1);

                //adding an address
                String addressSQL = "insert into address values (?,?,?,?,?,?,?,?,?,?)";
                stmt = conn.prepareStatement(addressSQL, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, newID);
                stmt.setString(2, address1);
                stmt.setString(3, address2);
                stmt.setInt(4, cityID);
                stmt.setString(5, postalCode);
                stmt.setString(6, phone);
                stmt.setTimestamp(7, currentTS);
                stmt.setString(8, currentUser);
                stmt.setTimestamp(9, currentTS);
                stmt.setString(10, currentUser);

                stmt.executeUpdate();

                rs = stmt.getGeneratedKeys();
                rs.first();
                int addressID = rs.getInt(1);

                //adding a customer
                String customerSQL = "insert into customer values (?,?,?,?,?,?,?,?)";

                stmt = conn.prepareStatement(customerSQL);
                stmt.setInt(1, newID);
                stmt.setString(2, name);
                stmt.setInt(3, addressID);
                stmt.setInt(4, active);
                stmt.setTimestamp(5, currentTS);
                stmt.setString(6, currentUser);
                stmt.setTimestamp(7, currentTS);
                stmt.setString(8, currentUser);

                stmt.executeUpdate();

                System.out.println("Customer added.");

                Stage stage = (Stage) addCustomerSave.getScene().getWindow();
                stage.close();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    //closes the window
    @FXML
    private void cancelButtonClicked() {

        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
