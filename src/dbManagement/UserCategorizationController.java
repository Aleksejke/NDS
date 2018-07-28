package dbManagement;

import application.Controller;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import database.DatabaseHandler;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class UserCategorizationController implements Initializable {
    
    ObservableList<CustomersCategorizationList> list = FXCollections.observableArrayList();
    
    @FXML
    private TableView<CustomersCategorizationList> tableView;
    @FXML
    private TableColumn<CustomersCategorizationList, String> idCol;
    @FXML
    private TableColumn<CustomersCategorizationList, String> nameCol;
    @FXML
    private TableColumn<CustomersCategorizationList, String> surnameCol;
    @FXML
    private TableColumn<CustomersCategorizationList, String> categoryCol;
    @FXML
    private TableColumn<CustomersCategorizationList, String> selectCol;
       
    
    ObservableList<CategoriesList> categoryList = FXCollections.observableArrayList();
    
    @FXML
    private TableView<CategoriesList> removeCategoryTable;
    @FXML
    private TableColumn<CategoriesList, String> removeCategoryCol;
    
    DatabaseHandler handler;
    
    @FXML
    private AnchorPane tablePaneCtg;
    @FXML
    private JFXButton clearFieldBtn;
    @FXML
    private JFXTextField categoryName;
    @FXML
    private ChoiceBox<String> categoriesChoiceBox;
    @FXML
    private Button categoryTooltip;

    public ChoiceBox<String> getCategoriesChoiceBox() {
        return categoriesChoiceBox;
    }
      
    @Override
    public void initialize(URL url, ResourceBundle rb) {
       initCol();
       loadData();
       initCategoryCol();
       loadCategories();
       tooltip();
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
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        selectCol.setCellValueFactory(new PropertyValueFactory<>("select"));
    }
    
    private void loadData() {
        handler = DatabaseHandler.getInstance();
        String query = "SELECT ID, Name, Surname, Category FROM CUSTOMERS";      
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
                String category = rs.getString("Category");
                
                list.add(new CustomersCategorizationList(id, name, surname, category));
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(AddUserController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null)
                try {
                    rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(RemoveUserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (!list.isEmpty()) {
            tableView.getItems().setAll(list);
        }
    }
    
    private void initCategoryCol() {
        removeCategoryCol.setCellValueFactory(new PropertyValueFactory<>("categories"));
    }
    
    private void loadCategories() {
        handler = DatabaseHandler.getInstance();
        String query = "SELECT * FROM CATEGORIES";
        ResultSet rs = handler.execQuery(query);
        try {
            while(rs.next()) {
                String categories = rs.getString("Category");
                categoryList.add(new CategoriesList(categories));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserCategorizationController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UserCategorizationController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (!categoryList.isEmpty()) {
            removeCategoryTable.getItems().setAll(categoryList); 
            for (CategoriesList ctgList : categoryList) {
                categoriesChoiceBox.getItems().add(ctgList.getCategories());   
            }
            categoriesChoiceBox.setValue(categoryList.get(0).getCategories());
        }
    }
    
    private void updateCategories() {
        categoriesChoiceBox.getItems().clear();
        categoryList.clear();
        loadCategories();
    }
    
    private void updateTableView() {
        list.clear();
        loadData();
    }
        
    private void tooltip() {
        final Tooltip t = new Tooltip();
        t.setText("Clear the text field");
        clearFieldBtn.setTooltip(t);
        final Tooltip info = new Tooltip();
        info.setText("Each customer can belong only to 3 categories");
        categoryTooltip.setTooltip(info);
    }
    
    @FXML
    private void editCustomerCategory(ActionEvent event) {
        CustomersCategorizationList editCategory = tableView.getSelectionModel().getSelectedItem();
        if (editCategory != null) {
            if (!editCategory.getCategory().equals("None")) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("editCustomerCategory.fxml"));
                    Parent parent = loader.load();

                    EditCustomerCategoryController controller = (EditCustomerCategoryController) loader.getController();
                    controller.editCheckBoxes(editCategory);
                    
                    Stage stage = new Stage(StageStyle.UNDECORATED);
                    stage.setScene(new Scene(parent));
                    stage.show();
                    
                    stage.setOnHiding((e) -> {
                        updateTableView();
                    });
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                alertMessageLoad(Alert.AlertType.ERROR, "This customer does not belong to any category");
            }
        } else {
            alertMessageLoad(Alert.AlertType.ERROR, "Please select customer first");
        }
        
    }
    
    @FXML
    private void saveCategorization(ActionEvent event) throws SQLException {
        ArrayList<String> selectedCustomers = new ArrayList<>();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to update "
                + "the categories of selected customers?", ButtonType.YES, ButtonType.CANCEL);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.showAndWait();
        
        if (alert.getResult() == ButtonType.YES) {
            for (CustomersCategorizationList csl : list) {
                if (csl.getSelect().isSelected()) {
                    selectedCustomers.add(csl.getId());
                }
            }   
            
            if (!selectedCustomers.isEmpty()) {
                String selectedCategoryName = categoriesChoiceBox.getSelectionModel().getSelectedItem();
                boolean completedUpdate = false;
                
                for (String sc : selectedCustomers) {
                    int selectedID = Integer.parseInt(sc.substring(1));
                    String existingCategory = "";
                    ResultSet rs = handler.execQuery("SELECT Category FROM CUSTOMERS WHERE ID = " + selectedID + "");
                    while (rs.next()) {
                        existingCategory = rs.getString("Category");
                    }
                    if (rs != null) {
                        rs.close();
                    }
                    
                    int existingCategoriesNumber = 0;
                    for (char c : existingCategory.toCharArray()) {
                        if (c == ',') {
                            existingCategoriesNumber++;
                        }
                    }
                    
                    if (existingCategoriesNumber < 2) {

                        if (!existingCategory.contains(selectedCategoryName)) {

                            if (!existingCategory.equals("None")) {
                                String action = "UPDATE CUSTOMERS SET Category = '"
                                        + existingCategory.concat(", ").concat(selectedCategoryName)
                                        + "' WHERE ID = " + selectedID + "";
                                handler.execAction(action);
                                completedUpdate = true;

                            } else {
                                String action = "UPDATE CUSTOMERS SET Category = '"
                                        + selectedCategoryName + "' WHERE ID = " + selectedID + "";
                                handler.execAction(action);
                                completedUpdate = true;
                            }

                        } else {
                            alertMessageLoad(Alert.AlertType.ERROR, "Some customers already belong to the selected category");
                            break;
                        }

                    } else {
                        alertMessageLoad(Alert.AlertType.ERROR, "Each customer can belong to only 3 categories");
                        break;
                    }
                }
                
                if (completedUpdate) {
                    updateTableView();
                    alertMessageLoad(Alert.AlertType.INFORMATION, "Updates successfully completed");
                }
            
            } else {
                alertMessageLoad(Alert.AlertType.ERROR, "Please select customers");
            }
        }
    }

    @FXML
    private void addNewCategory(ActionEvent event) throws SQLException {
        String newCategory = categoryName.getText();
        boolean exists = false;
        
        ResultSet rs = handler.execQuery("SELECT * FROM CATEGORIES WHERE Category = '" + newCategory + "'");
        while (rs.next()) {
            exists = true;
        }
        if (rs != null) {
            rs.close();
        }
        
        if (newCategory.isEmpty()) {
            alertMessageLoad(Alert.AlertType.ERROR, "The category name can not be empty");
            return;
        }
        
        if (exists) {
            alertMessageLoad(Alert.AlertType.ERROR, "The entered Category already exist");
            return;
        }
        
        String action = "INSERT INTO CATEGORIES VALUES ('" + newCategory + "')";
        if(handler.execAction(action)) {
            updateCategories();
            categoryName.clear();
            alertMessageLoad(Alert.AlertType.INFORMATION, "A new category has been added successfully");
        } else {
            alertMessageLoad(Alert.AlertType.ERROR, "Oops, some error occured");
        }
    }

    @FXML
    private void clearField(ActionEvent event) {
        categoryName.clear();
    }

    @FXML
    private void deleteCategory(ActionEvent event) throws SQLException {
        if (!removeCategoryTable.getSelectionModel().isEmpty()) {
            String nameOfCategory = removeCategoryTable.getSelectionModel().getSelectedItem().getCategories();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove this category?",
                    ButtonType.YES, ButtonType.CANCEL);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
            alert.setHeaderText(null);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                String actionDelete = "DELETE FROM CATEGORIES WHERE Category = '" + nameOfCategory + "'";
                String existingCategory;

                ResultSet rs = handler.execQuery("SELECT ID, Category FROM CUSTOMERS");
                while (rs.next()) {
                    int existingID = rs.getInt("ID");
                    existingCategory = rs.getString("Category");

                    if (existingCategory.equals(nameOfCategory)) {
                        String actionUpdate = "UPDATE CUSTOMERS SET Category = 'None' WHERE Category = '"
                                + nameOfCategory + "'";
                        handler.execAction(actionUpdate);

                    } else {

                        if (existingCategory.contains(nameOfCategory)) {

                            if (existingCategory.indexOf(nameOfCategory) != 0) {
                                existingCategory = existingCategory.substring(0, existingCategory.indexOf(nameOfCategory) - 2)
                                        .concat(existingCategory.substring(existingCategory.indexOf(nameOfCategory)
                                                + nameOfCategory.length()));
                            } else {
                                existingCategory = existingCategory.substring(nameOfCategory.length() + 2);
                            }

                            String actionUpdate = "UPDATE CUSTOMERS SET Category = '"
                                    + existingCategory + "' WHERE ID = " + existingID + "";
                            handler.execAction(actionUpdate);
                        }
                    }
                }

                if (rs != null) {
                    rs.close();
                }

                if (handler.execAction(actionDelete)) {
                    removeCategoryTable.getItems().removeAll(removeCategoryTable.getSelectionModel().getSelectedItem());
                    updateCategories();
                    updateTableView();
                    alertMessageLoad(Alert.AlertType.INFORMATION, "The category was successfully deleted");
                }
            }
        } else {
            alertMessageLoad(Alert.AlertType.ERROR, "Please select a category first");
        }
    }

    @FXML
    private void minimizeCaregorization(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void cancelCategorization(ActionEvent event) {
        list.clear();
        categoryList.clear();
        ((Stage) tablePaneCtg.getScene().getWindow()).close();
    }  
    
    public static class CustomersCategorizationList {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty surname;
        private final SimpleStringProperty category;
        private JFXCheckBox select;

        public CustomersCategorizationList(String id, String name, String surname, String category) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.surname = new SimpleStringProperty(surname);
            this.category = new SimpleStringProperty(category);
            this.select = new JFXCheckBox();
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

        public String getCategory() {
            return category.get();
        }

        public JFXCheckBox getSelect() {
            return select;
        }

        public void setSelect(JFXCheckBox select) {
            this.select = select;
        }
        
    }
    
    public static class CategoriesList {
        private final SimpleStringProperty categories;

        public CategoriesList(String categories) {
            this.categories = new SimpleStringProperty(categories);
        }

        public String getCategories() {
            return categories.get();
        }
              
    }
}
