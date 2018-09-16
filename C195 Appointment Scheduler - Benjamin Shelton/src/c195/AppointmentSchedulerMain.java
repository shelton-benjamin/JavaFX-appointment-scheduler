/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195;

import c195.Utilities.DBConnect;
import static c195.Utilities.UserLocaleTime.setDefaultLocale;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Benjamin
 *
 *
 */
public class AppointmentSchedulerMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        String title;
        ResourceBundle rb = null;
        setDefaultLocale();

        try {
            rb = ResourceBundle.getBundle("c195/Resources/login", Locale.getDefault());
        } catch (MissingResourceException ex) {

            rb = ResourceBundle.getBundle("login");

        } finally {

            title = rb.getString("loginTitle");

        }

        Parent root = FXMLLoader.load(getClass().getResource("View_Controller/Appointment Scheduler Login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DBConnect.doConnect();
        launch(args);
        DBConnect.closeConnect();
    }

}
