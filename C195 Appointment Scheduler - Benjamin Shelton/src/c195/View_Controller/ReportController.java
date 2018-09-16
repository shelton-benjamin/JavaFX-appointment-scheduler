/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195.View_Controller;

import static c195.View_Controller.MainScreenController.getReport;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Benjamin
 * 
 * screen with a text area to show the body of reports generated on Main Screen
 */
public class ReportController implements Initializable {
    
    @FXML
    private TextArea reportText;
    @FXML
    private Button okButton;
    
    //closes window
    @FXML
    private void closeWindow() {
        
        
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //grabs report text and fills text area with it
        reportText.setText(getReport());
    }    
    
}
