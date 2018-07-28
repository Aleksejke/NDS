package dbManagement;

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

public class AddUserController implements Initializable {

    @FXML
    private JFXTextField name;
    @FXML
    private JFXTextField surname;
    @FXML
    private JFXTextField email;
    @FXML
    private AnchorPane addNewCstmr;

    private boolean flag = false;

    DatabaseHandler handler;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handler = DatabaseHandler.getInstance();
        checkData();
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
    private void saveNewUser(ActionEvent event) {
        String userName = name.getText();
        String userSurname = surname.getText();
        String userEmail = email.getText();
        String userCategory = "None";

        int numberOfEta = 0;
        for (char ch : userEmail.toCharArray()) {
            if (ch == '@') {
                numberOfEta++;
            }
        }

        if (userName.isEmpty() || userSurname.isEmpty() || userEmail.isEmpty()) {
            alertMessageLoad(Alert.AlertType.ERROR, "Please fill in all fields");
            return;
        }

        if (userEmail.contains(" ") || (numberOfEta != 1)) {
            alertMessageLoad(Alert.AlertType.ERROR, "Wrong email address");
            return;
        } else {
            if (!userEmail.substring(userEmail.indexOf("@")).contains(".")
                    || !userEmail.substring(userEmail.indexOf("@") + 1, userEmail.indexOf("@") + 2).matches("[A-Za-z0-9]")) {
                alertMessageLoad(Alert.AlertType.ERROR, "Please check entered email address");
                return;
            }
        }

        if (!flag) {
            try {
                handler.execUpdate("ALTER TABLE CUSTOMERS ALTER COLUMN ID RESTART WITH 1");
                flag = true;
            } catch (SQLException ex) {
                Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                int lastAutoIncementIndex = 0;
                String query = "SELECT MAX(ID) as maks FROM CUSTOMERS";
                try (ResultSet rs = handler.execQuery(query)) {
                    while (rs.next()) {
                        lastAutoIncementIndex = Integer.parseInt(rs.getString("maks")) + 1;
                    }
                    if (rs != null) {
                        rs.close();
                    }
                }
                handler.execUpdate("ALTER TABLE CUSTOMERS ALTER COLUMN ID RESTART WITH " + lastAutoIncementIndex);
            } catch (SQLException ex) {
                Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String action = "INSERT INTO CUSTOMERS (Name, Surname, Email, Category) VALUES ("
                + "'" + userName + "',"
                + "'" + userSurname + "',"
                + "'" + userEmail + "',"
                + "'" + userCategory + "'"
                + ")";
        if (handler.execAction(action)) {
            name.setText("");
            surname.setText("");
            email.setText("");
            alertMessageLoad(Alert.AlertType.INFORMATION, "A new customer has been added to the database");
        } else {
            alertMessageLoad(Alert.AlertType.ERROR, "Oops, some error occured");
        }
    }

    @FXML
    private void cancelAddUser(ActionEvent event) {
        ((Stage) addNewCstmr.getScene().getWindow()).close();
    }

    private void checkData() {
        String query = "SELECT Name, Surname, Email FROM CUSTOMERS";
        ResultSet rs = handler.execQuery(query);
        try {
            if (rs.next() != false) {
                flag = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
