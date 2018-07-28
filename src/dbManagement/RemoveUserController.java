package dbManagement;

import database.DatabaseHandler;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


public class RemoveUserController implements Initializable {
    
    ObservableList<CustomersRemoveList> list = FXCollections.observableArrayList();

    @FXML
    private AnchorPane tablePane;
    @FXML
    private TableView<CustomersRemoveList> tableView;
    @FXML
    private TableColumn<CustomersRemoveList, String> idCol;
    @FXML
    private TableColumn<CustomersRemoveList, String> nameCol;
    @FXML
    private TableColumn<CustomersRemoveList, String> surnameCol;
    @FXML
    private TableColumn<CustomersRemoveList, String> emailCol;
    @FXML
    private TableColumn<CustomersRemoveList, String> categoryCol;
    
    DatabaseHandler handler;
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initCol();
        loadData();
    }   
    
    private void alertMessageLoad(Alert.AlertType type, String text) {
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();    
    }  

    private void initCol() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
    }

    private void loadData() {
        handler = DatabaseHandler.getInstance();
        String query = "SELECT * FROM CUSTOMERS";
        ResultSet rs = handler.execQuery(query);
        try {
            while (rs.next()) {
                
                String id = rs.getString("ID");
                
                if ((Integer.parseInt(id) / 100) == 0) {
                    if ((Integer.parseInt(id) / 10) == 0) {
                        id = "C00" + id;
                    } else
                        id = "C0" + id;
                } else id = "C" + id;
                
                String name = rs.getString("Name");
                String surname = rs.getString("Surname");
                String email = rs.getString("Email");
                String category = rs.getString("Category");
                
                list.add(new CustomersRemoveList(id, name, surname, email, category));
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(RemoveUserController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(RemoveUserController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (!list.isEmpty()) {
            tableView.setItems(list);
        }
    }

    @FXML
    private void removeUser(ActionEvent event) throws SQLException {      
        if (!tableView.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove this customer?",
                    ButtonType.YES, ButtonType.CANCEL);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
            alert.setHeaderText(null);
            alert.showAndWait();
            
            if (alert.getResult() == ButtonType.YES) {
                String id = tableView.getSelectionModel().getSelectedItem().getId().substring(1, 4);
                String action = "DELETE FROM CUSTOMERS WHERE ID = " + Integer.parseInt(id) + "";
                if (handler.execAction(action)) {
                    list.remove(tableView.getSelectionModel().getSelectedItem());
                    alertMessageLoad(Alert.AlertType.INFORMATION, "The customer was removed from the database");
                }
            }
        } else {
            alertMessageLoad(Alert.AlertType.ERROR, "Please select the customer first");
        }
    }

    @FXML
    private void cancelRemoveUser(ActionEvent event) {
        ((Stage) tablePane.getScene().getWindow()).close();
    }
    
    public static class CustomersRemoveList {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty surname;
        private final SimpleStringProperty email;
        private final SimpleStringProperty category;

        public CustomersRemoveList(String id, String name, String surname, String email, String category) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.surname = new SimpleStringProperty(surname);
            this.email = new SimpleStringProperty(email);
            this.category = new SimpleStringProperty(category);
        }

        public String getId() {
            return id.get();
        }

        public String getName() {
            return name.get();
        }

        public String getSurname() {
            return surname.get();
        }

        public String getEmail() {
            return email.get();
        }

        public String getCategory() {
            return category.get();
        }
        
        
    }
    
}
