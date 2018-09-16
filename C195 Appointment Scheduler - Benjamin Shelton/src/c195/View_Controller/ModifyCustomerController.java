/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import c195.Model.Customer;
import static c195.Model.Customer.customerValuesChecker;
import static c195.Utilities.DBConnect.getConnection;
import static c195.Utilities.UserLocaleTime.getCurrentTimeStamp;
import static c195.Utilities.UserLocaleTime.getCurrentUser;
import static c195.View_Controller.MainScreenController.getModifyCustomer;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ModifyCustomerController implements Initializable {

    @FXML
    private TextField modifyCustomerName;
    @FXML
    private TextField modifyCustomerAddressLine1;
    @FXML
    private TextField modifyCustomerAddressLine2;
    @FXML
    private TextField modifyCustomerCity;
    @FXML
    private TextField modifyCustomerPostalCode;
    @FXML
    private TextField modifyCustomerCountry;
    @FXML
    private TextField modifyCustomerPhone;
    @FXML
    private Button modifyCustomerSave;
    @FXML
    private Button cancelButton;

    private int customerID;
    private int addressID;
    private int cityID;
    private int countryID;

    //retrieves customer and address information from database
    private void getCustomer(Customer customer) throws SQLException {

        customerID = customer.getCustomerId();
        addressID = customer.getAddressID();
        String name = customer.getCustomerName();

        Connection conn = getConnection();

        String SQL = "select address, address2, cityId, postalCode, phone from address where addressId = ?";
        PreparedStatement stmt = conn.prepareStatement(SQL);
        stmt.setInt(1, addressID);
        stmt.executeQuery();

        ResultSet rs = stmt.getResultSet();
        rs.next();

        String address1 = rs.getString(1);
        String address2 = rs.getString(2);
        cityID = rs.getInt(3);
        String postalCode = rs.getString(4);
        String phone = rs.getString(5);

        SQL = "select city, countryId from city where cityId = ?";
        stmt = conn.prepareStatement(SQL);
        stmt.setInt(1, cityID);
        stmt.executeQuery();

        rs = stmt.getResultSet();
        rs.next();

        String city = rs.getString(1);
        countryID = rs.getInt(2);

        SQL = "select country from country where countryId = ?";
        stmt = conn.prepareStatement(SQL);
        stmt.setInt(1, countryID);
        stmt.executeQuery();

        rs = stmt.getResultSet();
        rs.next();

        String country = rs.getString(1);

        fillFields(name, address1, address2, city, postalCode, country, phone);

    }

    //prepopulates each field with saved customer info from database
    private void fillFields(String name, String address1, String address2,
            String city, String postalCode, String country, String phone) {

        modifyCustomerName.setText(name);
        modifyCustomerAddressLine1.setText(address1);
        modifyCustomerAddressLine2.setText(address2);
        modifyCustomerCity.setText(city);
        modifyCustomerPostalCode.setText(postalCode);
        modifyCustomerCountry.setText(country);
        modifyCustomerPhone.setText(phone);

    }

    //saves the changes to the selected customer
    //if all input is valid
    @FXML
    private void modifySaveButtonClicked() throws Exception {

        String name = modifyCustomerName.getText();
        String address1 = modifyCustomerAddressLine1.getText();
        String address2 = modifyCustomerAddressLine2.getText();
        String city = modifyCustomerCity.getText();
        String postalCode = modifyCustomerPostalCode.getText();
        String country = modifyCustomerCountry.getText();
        String phone = modifyCustomerPhone.getText();

        boolean validInput = customerValuesChecker(name, address1, city,
                postalCode, country, phone);

        if (validInput) {

            Connection conn = getConnection();
            PreparedStatement stmt;

            Timestamp currentTS = getCurrentTimeStamp();
            String currentUser = getCurrentUser();

            String SQL = "update customer set customerName = ?, lastUpdate = ?, "
                    + "lastUpdateBy = ? where customerId = ?";
            stmt = conn.prepareStatement(SQL);
            stmt.setString(1, name);
            stmt.setTimestamp(2, currentTS);
            stmt.setString(3, currentUser);
            stmt.setInt(4, customerID);
            stmt.executeUpdate();

            SQL = "update address set address = ?, address2 = ?, postalCode = ?, "
                    + "phone = ?, lastUpdate = ?, "
                    + "lastUpdateby = ? where addressId = ?";
            stmt = conn.prepareStatement(SQL);
            stmt.setString(1, address1);
            stmt.setString(2, address2);
            stmt.setString(3, postalCode);
            stmt.setString(4, phone);
            stmt.setTimestamp(5, currentTS);
            stmt.setString(6, currentUser);
            stmt.setInt(7, addressID);
            stmt.executeUpdate();

            SQL = "update city set city = ?, lastUpdate = ?, lastUpdateby = ? where cityId = ?";
            stmt = conn.prepareStatement(SQL);
            stmt.setString(1, city);
            stmt.setTimestamp(2, currentTS);
            stmt.setString(3, currentUser);
            stmt.setInt(4, cityID);
            stmt.executeUpdate();

            SQL = "update country set country = ?, lastUpdate = ?, lastUpdateby = ? where countryId = ?";
            stmt = conn.prepareStatement(SQL);
            stmt.setString(1, country);
            stmt.setTimestamp(2, currentTS);
            stmt.setString(3, currentUser);
            stmt.setInt(4, countryID);
            stmt.executeUpdate();

            System.out.println("Customer updated.");

            Stage stage = (Stage) modifyCustomerSave.getScene().getWindow();
            stage.close();

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

        try {
            getCustomer(getModifyCustomer());
        } catch (SQLException ex) {
            Logger.getLogger(ModifyCustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
