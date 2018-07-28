package dbManagement;

import com.jfoenix.controls.JFXCheckBox;
import database.DatabaseHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class EditCustomerCategoryController implements Initializable {
       
    @FXML
    private AnchorPane categoriesEditPane;
    @FXML
    private JFXCheckBox firstCategory;
    @FXML
    private JFXCheckBox secondCategory;
    @FXML
    private JFXCheckBox thirdCategory;
    
    private int categoriesNumber;
    private int customerId;
    private String categories;
    
    DatabaseHandler handler;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handler = DatabaseHandler.getInstance();
        categoriesEditPane.setStyle("-fx-background-color: #e6f1f2");
    }

    private void alertMessageLoad(AlertType type, String text) {
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();    
    }      

    @FXML
    private void cancelCategoryChanges(ActionEvent event) {
        ((Stage) categoriesEditPane.getScene().getWindow()).close();
    }

    public void editCheckBoxes(UserCategorizationController.CustomersCategorizationList ccl) {
        customerId = Integer.parseInt(ccl.getId().substring(1, ccl.getId().length()));
        categories = ccl.getCategory();
        
        if (categories.contains(",")) {
            categoriesNumber = categories.length() - categories.replaceAll(",","").length();
            
            if (categoriesNumber == 2) {
                firstCategory.setText(categories.substring(0, categories.indexOf(',')));
                secondCategory.setText(categories.substring(categories.indexOf(',') + 2,
                        categories.indexOf(',', categories.indexOf(',') + 1)));
                thirdCategory.setText(categories.substring(categories.indexOf(',', categories.indexOf(',') + 1) + 2,
                        categories.length()));
            
            } else {
                firstCategory.setText(ccl.getCategory().substring(0, ccl.getCategory().indexOf(',')));
                secondCategory.setText(ccl.getCategory().substring(ccl.getCategory().indexOf(',') + 2,
                        ccl.getCategory().length()));
                thirdCategory.setOpacity(0);
                thirdCategory.setDisable(true);
            }
        
        } else {
            firstCategory.setText(categories);
            secondCategory.setOpacity(0);
            secondCategory.setDisable(true);
            thirdCategory.setOpacity(0);
            thirdCategory.setDisable(true);
        }
    }

    @FXML
    private void removeCustomerCategory(ActionEvent event) {

        if (firstCategory.isSelected() || secondCategory.isSelected() || thirdCategory.isSelected()) {
            
            String action = "";
            switch (categoriesNumber) {
                case 0:
                    if (firstCategory.isSelected()) {
                        action = "UPDATE CUSTOMERS SET Category = 'None' WHERE ID = " + customerId;
                    }
                    break;

                case 1:
                    if (firstCategory.isSelected() && secondCategory.isSelected()) {
                        action = "UPDATE CUSTOMERS SET Category = 'None' WHERE ID = " + customerId;
                        break;
                    } else {

                        if (firstCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(categories.indexOf(',') + 2, categories.length())
                                    + "' WHERE ID = " + customerId;
                            break;
                        }

                        if (secondCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(0, categories.indexOf(',')) + "' WHERE ID = " + customerId;
                        }
                    }
                    break;

                case 2:
                    if (firstCategory.isSelected() && secondCategory.isSelected() && thirdCategory.isSelected()) {
                        action = "UPDATE CUSTOMERS SET Category = 'None' WHERE ID = " + customerId;
                        break;
                    } else {

                        if (firstCategory.isSelected() && secondCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(categories.indexOf(',', categories.indexOf(',') + 1) + 2,
                                            categories.length()) + "' WHERE ID = " + customerId;
                            break;
                        }

                        if (firstCategory.isSelected() && thirdCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(categories.indexOf(',') + 2,
                                            categories.indexOf(',', categories.indexOf(',') + 1))
                                    + "' WHERE ID = " + customerId;
                            break;
                        }

                        if (secondCategory.isSelected() && thirdCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(0, categories.indexOf(',')) 
                                    + "' WHERE ID = " + customerId;
                            break;
                        }

                        if (firstCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(categories.indexOf(',') + 2, categories.length())
                                    + "' WHERE ID = " + customerId;
                            break;
                        }

                        if (secondCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(0, categories.indexOf(',')).concat(categories.substring(
                                            categories.indexOf(',', categories.indexOf(',') + 1), categories.length()))
                                    + "' WHERE ID = " + customerId;
                            break;
                        }

                        if (thirdCategory.isSelected()) {
                            action = "UPDATE CUSTOMERS SET Category = '"
                                    + categories.substring(0, categories.indexOf(',', categories.indexOf(',') + 1))
                                    + "' WHERE ID = " + customerId;
                        }
                    }
            }
            
            if (handler.execAction(action)) {
                alertMessageLoad(AlertType.INFORMATION, "The changes have been made successfully");
                ((Stage) categoriesEditPane.getScene().getWindow()).close();
            }
            
        } else {
            alertMessageLoad(AlertType.ERROR, "Please select a category for removal");
        }
    }

}
