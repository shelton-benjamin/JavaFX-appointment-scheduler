/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.Model;

import java.util.Formatter;
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
public class Customer {

    private IntegerProperty customerId;
    private StringProperty customerName;
    private int addressID;

    public Customer() {

        customerId = new SimpleIntegerProperty();
        customerName = new SimpleStringProperty();

    }

    //getters and setters
    public int getCustomerId() {

        return customerId.get();
    }

    public void setCustomerId(int customerId) {
        this.customerId.set(customerId);
    }

    public IntegerProperty CustomerIdProperty() {
        return customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }

    public String getCustomerName() {

        return customerName.get();
    }

    public StringProperty CustomerNameProperty() {
        return customerName;
    }

    public int getAddressID() {
        return addressID;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    //ensures each customer has complete and valid information
    public static boolean customerValuesChecker(String name, String address, String city,
            String postalCode, String country, String phone) {

        String message = "%s is a required field.";
        String field = null;
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);

        String header = "Invalid input";

        try {
            field = "Name";
            String test = name.substring(0, 1);

            field = "Address Line 1";
            test = address.substring(0, 1);

            field = "City";
            test = city.substring(0, 1);

            field = "Postal Code";
            test = postalCode.substring(0, 1);

            field = "Country";
            test = country.substring(0, 1);

            field = "Phone";
            test = phone.substring(0, 1);

            //exception thrown if any required fields are blank
        } catch (StringIndexOutOfBoundsException ex) {

            fmt.format(message, field);
            JOptionPane.showMessageDialog(null, sbuf.toString(), header, 0);
            return false;
        }

        try {

            String test = phone.substring(0, 10);
            long digits = Long.valueOf(phone);

            //exception thrown if phone number contains anything other than digits
            //and is not at least 10 digits long
        } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {

            String phoneMessage = "Phone number must be at least "
                    + "10 digits long with no letters, spacing, or punctation.";

            JOptionPane.showMessageDialog(null, phoneMessage, header, 0);
            return false;
        }

        //returns true if all checks are met
        return true;
    }

}
