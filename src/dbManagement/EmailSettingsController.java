package dbManagement;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import database.DatabaseHandler;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class EmailSettingsController implements Initializable {

    @FXML
    private JFXTextField email;
    @FXML
    private JFXPasswordField password;
    @FXML
    private AnchorPane emailSettings;
    DatabaseHandler handler;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handler = DatabaseHandler.getInstance();
    }  
    
    private void alertMessageLoad(Alert.AlertType type, String text) {
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();    
    } 

    @FXML
    private void updateEmailSettings(ActionEvent event) {
        String emailAddress = email.getText();
        String emailPassword = password.getText();

        int numberOfEta = 0;
        for (char ch : emailAddress.toCharArray()) {
            if (ch == '@') {
                numberOfEta++;
            }
        }

        if (emailAddress.isEmpty() || emailPassword.isEmpty()) {
            alertMessageLoad(Alert.AlertType.ERROR, "Please fill in all fields");
            return;
        }

        if (emailAddress.contains(" ") || (numberOfEta != 1)) {
            alertMessageLoad(Alert.AlertType.ERROR, "Wrong email address");
            return;
        } else {
            if (!emailAddress.substring(emailAddress.indexOf("@")).contains(".")
                    || !emailAddress.substring(emailAddress.indexOf("@") + 1, emailAddress.indexOf("@") + 2).matches("[A-Za-z0-9]")) {
                alertMessageLoad(Alert.AlertType.ERROR, "Please check entered email address");
                return;
            }
        }

        ResultSet rs = handler.execQuery("SELECT Login FROM EMAILLOGIN");
        try {
            if (!rs.next()) {
                handler.execAction("INSERT INTO EMAILLOGIN VALUES ('" + emailAddress + "', '" + emailPassword + "') ");
                clearFields();
                alertMessageLoad(Alert.AlertType.INFORMATION, "Your data has been successfully saved");
                
            } else {
                String previousLogin = "";
                rs = handler.execQuery("SELECT Login FROM EMAILLOGIN");
                while (rs.next()) {
                    previousLogin = rs.getString("Login");
                }
                
                handler.execAction("UPDATE EMAILLOGIN SET Login = '" + emailAddress + "',"
                        + "Password = '" + emailPassword + "'"
                        + "WHERE Login = '" + previousLogin + "'");
                
                clearFields();
                alertMessageLoad(Alert.AlertType.INFORMATION, "Your data has been successfully updated");
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(EmailSettingsController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(EmailSettingsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void clearFields() {
        email.clear();
        password.clear();
    }
    
    @FXML
    private void cancelEmailSettings(ActionEvent event) {
        ((Stage) emailSettings.getScene().getWindow()).close();
    }
    
}
