package recipients;

import application.Controller;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import database.DatabaseHandler;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class RecipientsListController implements Initializable {

    ObservableList<recipientsList> list = FXCollections.observableArrayList();

    @FXML
    private AnchorPane recipientSelectionAnchor;
    @FXML
    private TableView<recipientsList> recipientsTable;
    @FXML
    private TableColumn<recipientsList, String> infoCol;
    @FXML
    private TableColumn<recipientsList, String> selectCol;

    @FXML
    private JFXRadioButton categoryRadioBtn;
    @FXML
    private JFXRadioButton customersRadioBtn;
    
    DatabaseHandler handler;

    private int selectedOption = 1;
    private String selectedCategories = "";
    private String selectedCustomers = "";
    Controller mainController = new Controller();
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCol();
        radioBtnGroup();
        loadCategoryData();
        tooltip();
    }
    
    private void initCol() {
        selectCol.setCellValueFactory(new PropertyValueFactory<>("select"));
        infoCol.setCellValueFactory(new PropertyValueFactory<>("information"));
    }
    
    @FXML
    private void selectRecipients(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to select these categories/customers?",
                ButtonType.YES, ButtonType.CANCEL);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            boolean selected = false;
            if (selectedOption == 1) {
                selectedCategories = "";
                int counter = 0;
                for (recipientsList rl : list) {
                    if (rl.getSelect().isSelected()) {
                        counter++;
                        if (counter <= 5) {
                            selectedCategories = selectedCategories.concat(rl.getInformation()).concat(",");
                        } else {
                            selectedCategories = "";
                            break;
                        }
                    }
                }

                if (counter <= 5 && !selectedCategories.isEmpty()) {
                    selectedCategories = "Cat:".concat(selectedCategories.substring(0, selectedCategories.length() - 1));
                    selected = true;
                } else {
                    if (counter > 5) {
                        mainController.alertMessageLoad(Alert.AlertType.ERROR, "Only 5 categories can be selected at a time");
                    } else {
                        mainController.alertMessageLoad(Alert.AlertType.ERROR, "Please select Categories first");
                    }
                }
            }

            if (selectedOption == 2) {
                selectedCustomers = "";
                for (recipientsList rl : list) {
                    if (rl.getSelect().isSelected()) {
                        selectedCustomers = selectedCustomers.concat(rl.getInformation().substring(1, 4)).concat(",");
                    }
                }

                if (!selectedCustomers.isEmpty()) {
                    selectedCustomers = "Cus:".concat(selectedCustomers.substring(0, selectedCustomers.length() - 1)); 
                    selected = true;
                } else {
                    mainController.alertMessageLoad(Alert.AlertType.ERROR, "Please select Customers first");
                }
            }
            
            if (selected) {
                mainController.fillingToWhomField(selected, selectedOption);
                mainController.alertMessageLoad(Alert.AlertType.INFORMATION, "Selected categories/customers added to the recipients list");
            }
        }
    }

    @FXML
    private void cancelSelection(ActionEvent event) throws IOException {
        list.clear();
        
        if (!selectedCategories.isEmpty()) {
            mainController.setSelectedCategories(selectedCategories);
        }
        if (!selectedCustomers.isEmpty()) {
            mainController.setSelectedCustomers(selectedCustomers);
        }
        
        ((Stage) recipientSelectionAnchor.getScene().getWindow()).close();
    }

    private void loadCategoryData() {       
        updateData();
        handler = DatabaseHandler.getInstance();
        ResultSet rs = handler.execQuery("SELECT Category FROM CATEGORIES");
        
        try {
            while (rs.next()) {
                list.add(new recipientsList(rs.getString("Category")));
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(RecipientsListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!list.isEmpty()) {
            recipientsTable.getItems().setAll(list);
        }
    }

    private void loadCustomersData() {
        updateData();
        handler = DatabaseHandler.getInstance();
        ResultSet rs = handler.execQuery("SELECT ID, Name, Surname FROM CUSTOMERS");
        
        try {
            while (rs.next()) {
                String id = rs.getString("ID");

                if ((Integer.parseInt(id) / 100) == 0) {
                    if ((Integer.parseInt(id) / 10) == 0) {
                        id = "C00" + id;
                    } else {
                        id = "C0" + id;
                    }
                } else {
                    id = "C" + id;
                }

                list.add(new recipientsList(id.concat(", " + rs.getString("Name")).concat(" " + rs.getString("Surname"))));
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(RecipientsListController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (!list.isEmpty()) {
            recipientsTable.getItems().setAll(list);
        }
    }

    private void radioBtnGroup() {
        ToggleGroup toggleGroup = new ToggleGroup();
        categoryRadioBtn.setToggleGroup(toggleGroup);
        customersRadioBtn.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(categoryRadioBtn);
    }

    @FXML
    private void loadCategory(ActionEvent event) {
        infoCol.setText("Category");
        loadCategoryData();
        selectedOption = 1;
    }

    @FXML
    private void loadCustomers(ActionEvent event) {
        infoCol.setText("Customers");
        loadCustomersData();
        selectedOption = 2;
    }
    
    private void updateData() {
        list.clear();
    }
    
    private void tooltip() {
        final Tooltip t = new Tooltip();
        t.setText("Only 5 categories can be selected at a time");
        categoryRadioBtn.setTooltip(t);
    }
    
    public static class recipientsList {

        private final SimpleStringProperty information;
        private JFXCheckBox select;

        public recipientsList(String information) {
            this.information = new SimpleStringProperty(information);
            this.select = new JFXCheckBox();
        }

        public String getInformation() {
            return information.get();
        }

        public JFXCheckBox getSelect() {
            return select;
        }

        public void setSelect(JFXCheckBox select) {
            this.select = select;
        }

    }
    
}