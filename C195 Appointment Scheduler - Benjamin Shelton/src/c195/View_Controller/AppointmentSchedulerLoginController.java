/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import static c195.Utilities.Authenticator.authenticate;
import static c195.Utilities.DBConnect.getConnection;
import static c195.Utilities.Logger.addToLog;
import static c195.Utilities.UserLocaleTime.getCurrentTimeStamp;
import static c195.Utilities.UserLocaleTime.setCurrentUser;
import static c195.Utilities.UserLocaleTime.setDefaultLocale;
import static c195.Utilities.UserLocaleTime.setUserTimeZone;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Formatter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 * FXML Controller class
 *
 * @author Benjamin
 */
public class AppointmentSchedulerLoginController implements Initializable {
    
    @FXML
    private Label title;
    @FXML
    private Label loginUsernameLabel;
    @FXML
    private Label loginPasswordLabel;
    @FXML
    private TextField loginUsernameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Button loginSigninButton;
    @FXML
    private Button loginCancelButton;
    
    private ResourceBundle rb;
    
    
    //this method sets the text for all labels on login screen to user's locale
    private void setLabels() {
        
        try {
            //receives default locale from UserLocaleTime
            rb = ResourceBundle.getBundle("c195/Resources/login", Locale.getDefault());
        } catch (MissingResourceException ex) {
            
            //default resources used if language not supported
            rb = ResourceBundle.getBundle("login");
            
        } finally {
            loginUsernameLabel.setText(rb.getString("loginUsernameLabel"));
            loginPasswordLabel.setText(rb.getString("loginPasswordLabel"));
            loginSigninButton.setText(rb.getString("loginSigninButton"));
            loginCancelButton.setText(rb.getString("loginCancelButton"));
            title.setText(rb.getString("programTitle"));
        }
    }
    
    //signs user in if username and password are valid
    //logs the sign in attempt
    @FXML
    private void loginClicked() throws Exception {
        
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        String SQL = "select * from user";
        ResultSet rs = stmt.executeQuery(SQL);
        
        //grabs table of user info and username and password just supplied
        //passes to authenticator method to determine if user is in database
        String attemptedUsername = loginUsernameField.getText();
        String attemptedPassword = loginPasswordField.getText();
        boolean checksOut = authenticate(attemptedUsername, attemptedPassword, rs);
        
        StringBuilder logged = new StringBuilder();
        Formatter fmt = new Formatter(logged);
        Timestamp timestamp = getCurrentTimeStamp();
        
        //if user is in database, main screen is opened and sign in is logged
        if (checksOut) {
            
            fmt.format("Successful login.%nUsername: %s at %s", attemptedUsername, timestamp);
            addToLog(logged.toString());
            
            setCurrentUser(attemptedUsername);
            setUserTimeZone();
            
            System.out.println("Sign in successful.");
            openMainScreen();
            
        //if user is not in database, they receive an error message and attempted
        //sign in is logged
        } else {
            
            fmt.format("Unsuccessful login.%nUsername: %s at %s", attemptedUsername, timestamp);
            addToLog(logged.toString());
            
            String incorrect = rb.getString("incorrect");
            String header = rb.getString("header");
            JOptionPane.showMessageDialog(null, incorrect, header, JOptionPane.ERROR_MESSAGE);
            
            loginUsernameField.clear();
            loginPasswordField.clear();
            
            System.out.println("Incorrect username or password.");
        }
        
    }
    
    //closes program
    @FXML
    private void cancelClicked() throws Exception {
        Stage stage = (Stage) loginCancelButton.getScene().getWindow();
        stage.close();
        
    }
    
    //opens the main screen if the credentials are good
    private void openMainScreen() throws Exception {
        
        Stage stage1 = (Stage) loginSigninButton.getScene().getWindow();
        stage1.hide();
        Parent root = FXMLLoader.load(getClass().getResource("Main Screen.fxml"));
        Scene scene = new Scene(root);
        Stage stage2 = new Stage();
        stage2.setScene(scene);
        stage2.setTitle("Main Screen");
        stage2.show();
        
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        setDefaultLocale();
        setLabels();
    }
    
}
