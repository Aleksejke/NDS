package application;

import analytics.Analytics;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import database.DatabaseHandler;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Controller implements Initializable {

    private double xOffset = 0;
    private double yOffset = 0;
    private int customerNumber;
    private int emailNumber;
    private byte selectedTechnique = 1;
    private final File file = new File("src/analytics/startDateFile.txt");
    
    public static String selectedCategories = "";
    public static String selectedCustomers = "";
    public static JFXTextField toWhomSendList;

    ArrayList<Person> person;
    ArrayList<Emails> emails;
    
    DatabaseHandler handler;
    
    @FXML
    private PieChart statisticsChart;

    @FXML
    private JFXTextField toWhomSend;
    static String recipientsList;
    @FXML
    private JFXTextField notificationSubject;
    @FXML
    private TextArea notificationText;
    @FXML
    private JFXTextField searchPerson;
    @FXML
    private Text personName;
    @FXML
    private Text personSurname;
    @FXML
    private Text personEmail;
    @FXML
    private Text personCategory;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonPrevious;
    @FXML
    private Button addRecipientBtn;
    @FXML
    private JFXButton clearFieldBtn;
    @FXML
    private VBox lastSentEmail;
    @FXML
    private Text lastRecipient;
    @FXML
    private Text lastSubject;
    @FXML
    private Text lastDate;
    @FXML
    private JFXTextField searchEmail;
    @FXML
    private Text emailInfoTitle;
    @FXML
    private Button dateInfoBtn;
    @FXML
    private Button buttonNextEmail;
    @FXML
    private Button buttonPreviousEmail;
    @FXML
    private Tab statisticsTab;
    @FXML
    private FontAwesomeIconView chartImage;
    @FXML
    private Label chartLabel;

    
    /*Main window settings*/
    
    //Enable ENTER key in alert dialog
    EventHandler<KeyEvent> fireOnEnter = event -> {
        if (KeyCode.ENTER.equals(event.getCode())
                && event.getTarget() instanceof Button) {
            ((Button) event.getTarget()).fire();
        }
    };
    @FXML
    private JFXRadioButton webbeaconRadioBtn;
    @FXML
    private JFXRadioButton clicktrackingRadioBtn;
    
    public void alertMessageLoad(AlertType type, String text) {
        //image to the window
        Alert alert = new Alert(type);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();    
    }   
    
    @FXML
    public void exitApplication(ActionEvent event) {
        //Customized alert dialog
        ButtonType exit = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?", exit, ButtonType.CANCEL);
        alert.setTitle("Confirm Exit");
        alert.setHeaderText(null);

        //image to the confirmation window
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
 
        //Focused buttons fires by Enter key
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getButtonTypes().stream()
                .map(dialogPane::lookupButton)
                .forEach(button -> button.addEventHandler(KeyEvent.KEY_PRESSED, fireOnEnter));

        //Deactivate Default behavior for exit-Button
        Button exitButton = (Button) alert.getDialogPane().lookupButton(exit);
        exitButton.setDefaultButton(false);

        alert.showAndWait();
        if (alert.getResult() == exit) {
            Platform.exit();
            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
                DriverManager.getConnection("jdbc:derby:; shutdown=true");
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException ex) {
            
            }
        }
    }

    @FXML
    public void minimizeApplication(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        // is stage minimizable into task bar. (true | false)
        stage.setIconified(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        handler = DatabaseHandler.getInstance();
        tooltip();
        toWhomSendList = toWhomSend;
        lastSentEmailInfo();
        initGraph();
        radioBtnGroup();
    }
    
    private void tooltip() {
        final Tooltip t = new Tooltip();
        t.setText("Select recipients");
        addRecipientBtn.setTooltip(t);
        
        final Tooltip clear = new Tooltip();
        clear.setText("Clear To whom field");
        clearFieldBtn.setTooltip(clear);
        
        final Tooltip dateInfo = new Tooltip();
        dateInfo.setText("Date format: yyyy-mm-dd");
        dateInfoBtn.setTooltip(dateInfo);
    }
    
    
    /*Database Management section*/
    
    @FXML
    private void loadNewCstmr(ActionEvent event) {
        if (!searchPerson.getText().isEmpty()) {
            searchPerson.clear();
        }
        
        if (!personEmail.getText().equals("Email")) {
            clearSearchCache();
        }
        
        if (emailInfoTitle.getText().equals("The Found Information")) {
            clearEmailSearch();
        }
        
        loadWindow("/dbManagement/addUser.fxml");
    }

    @FXML
    private void loadRemoveCstmr(ActionEvent event) {
        if (!searchPerson.getText().isEmpty()) {
            searchPerson.clear();
        }
        
        if (!personEmail.getText().equals("Email")) {
            clearSearchCache();
        }
        
        if (emailInfoTitle.getText().equals("The Found Information")) {
            clearEmailSearch();
        }
        
        loadWindow("/dbManagement/removeUser.fxml");
    }

    @FXML
    private void loadCstmrCateg(ActionEvent event) {
        if (!searchPerson.getText().isEmpty()) {
            searchPerson.clear();
        }
        
        if (!personEmail.getText().equals("Email")) {
            clearSearchCache();
        }
        
        if (emailInfoTitle.getText().equals("The Found Information")) {
            clearEmailSearch();
        }
        
        loadWindow("/dbManagement/userCategorization.fxml");
    }

    @FXML
    private void selectRecipient(ActionEvent event) {
        loadWindow("/recipients/recipientsList.fxml");
    }

    void loadWindow(String loc) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource(loc));
            Stage stage = new Stage(StageStyle.UNDECORATED);

            //window movement
            parent.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            parent.setOnMouseDragged(event -> {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });

            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void searchInfo(ActionEvent event) throws SQLException {      
        boolean flag = false;
        clearSearchCache();
        
        if (!searchPerson.getText().isEmpty()) {
            person = new ArrayList<>();
            customerNumber = 0;
            Person someone;
            String query;
            String searchInput = searchPerson.getText();

            //id check
            if ((searchInput.length() == 4) && (searchInput.charAt(0) == 'C')
                    && searchInput.substring(1, 4).matches("[0-9]+") && !searchInput.contains(" ")) {
                query = "SELECT Name, Surname, Email, Category FROM CUSTOMERS WHERE ID = "
                        + Integer.parseInt(searchInput.substring(1, 4)) + "";
                ResultSet rs = handler.execQuery(query);
                while (rs.next()) {
                    setInfo(rs.getString("Name"), rs.getString("Surname"), rs.getString("Email"), rs.getString("Category"));
                    flag = true;
                }
                if (rs != null) {
                    rs.close();
                }

            //name or surname check
            } else {
                query = "SELECT Name, Surname, Email, Category FROM CUSTOMERS WHERE Name = '"
                        + searchInput + "' OR Surname = '" + searchInput + "'";
                ResultSet rs = handler.execQuery(query);
                while (rs.next()) {
                    someone = new Person();
                    someone.setName(rs.getString("Name"));
                    someone.setSurname(rs.getString("Surname"));
                    someone.setEmail(rs.getString("Email"));
                    someone.setCategory(rs.getString("Category"));
                    person.add(someone);
                }
                if (rs != null) {
                    rs.close();
                }

                if (person.size() == 1) {
                    setInfo(person.get(0).getName(), person.get(0).getSurname(),
                            person.get(0).getEmail(), person.get(0).getCategory());
                    flag = true;
                    person.clear();
                } else {
                    if (person.size() > 1) {
                        flag = true;
                        setInfo(person.get(0).getName(), person.get(0).getSurname(),
                                person.get(0).getEmail(), person.get(0).getCategory());
                        buttonNext.setOpacity(1);
                        buttonNext.setDisable(false);
                        buttonPrevious.setDisable(true);
                    }
                }
            }
        }
        
        if (!flag) {
            alertMessageLoad(AlertType.ERROR, "Nothing found");
        }
    }

    private void setInfo(String n, String s, String e, String c) {
        personName.setText(n);
        personSurname.setText(s);
        personEmail.setText(e);
        personCategory.setText(c);
    }

    private void clearSearchCache() {
        personName.setText("Name");
        personSurname.setText("Surname");
        personEmail.setText("Email");
        personCategory.setText("Category");
        buttonNext.setOpacity(0);
        buttonPrevious.setOpacity(0);
        buttonNext.setDisable(true);
        buttonPrevious.setDisable(true);
        
        if (person != null) {
            person.clear();
        }
    }

    @FXML
    private void nextCustromerInfo(ActionEvent event) {
        customerNumber++;
        changerOfCustomerInfo(customerNumber);
    }

    @FXML
    private void previousCustromerInfo(ActionEvent event) {
        customerNumber--;
        changerOfCustomerInfo(customerNumber);
    }

    private void changerOfCustomerInfo(int number) {
        if (number == (person.size() - 1)) {
            buttonNext.setOpacity(0);
            buttonNext.setDisable(true);
        } else {
            buttonNext.setOpacity(1);
            buttonNext.setDisable(false);
        }

        if (number == 0) {
            buttonPrevious.setOpacity(0);
            buttonPrevious.setDisable(true);
        } else {
            buttonPrevious.setOpacity(1);
            buttonPrevious.setDisable(false);
        }

        setInfo(person.get(number).getName(), person.get(number).getSurname(),
                person.get(number).getEmail(), person.get(number).getCategory());
    }

    
    /*Send a Notification section*/
    
    @FXML
    private void loadEmailSettings(ActionEvent event) {
        if (emailInfoTitle.getText().equals("The Found Information")) {
            clearEmailSearch();
        }
        
        loadWindow("/dbManagement/emailSettings.fxml");
    }
    
    @FXML
    private void sendMessage(ActionEvent event) throws IOException, MessagingException, SQLException {
        Alert alert;

        if (!notificationText.getText().isEmpty() && !notificationSubject.getText().isEmpty()
                && !toWhomSend.getText().isEmpty()) {
            
            ResultSet rs = handler.execQuery("SELECT Login FROM EMAILLOGIN");
            if (rs.next() == false) {
                alertMessageLoad(AlertType.ERROR, "Please configure email settings first");
                return;
            }
                       
            String user = "";
            String password = "";           

            rs = handler.execQuery("SELECT * FROM EMAILLOGIN");
            while(rs.next()) {
                user = rs.getString("Login");
                password = rs.getString("Password");
            }
            
            if (rs != null) {
                rs.close();
            }

            
            final Properties properties = System.getProperties();
            if (user.contains("gmail")) {
                properties.load(Controller.class.getClassLoader().getResourceAsStream("gmail.properties"));
            } else if (user.contains("outlook") || user.contains("hotmail")) {
                properties.load(Controller.class.getClassLoader().getResourceAsStream("outlook.properties"));
            } else if (user.contains("yahoo")) {
                properties.load(Controller.class.getClassLoader().getResourceAsStream("yahoo.properties"));
            }

            //security
            java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            
            LinkedHashSet<String> address = listOfRecipients();

            HashMap <String, String> map = new HashMap<>();
            
            int i = 1000;
            for (String s : address) {               
                rs = handler.execQuery("SELECT ID FROM CUSTOMERS WHERE Email = '" + s + "'");
                if (rs.next()) {
                    map.put(rs.getString("ID"), s);
                } else {
                    map.put(Integer.toString(i), s);
                    i++;
                }       
            }
            
            Session mailSession = Session.getDefaultInstance(properties);
            MimeMessage message;

            String notificationMessage = notificationText.getText().replaceAll("\n", "<br>");
                       
            Transport transport = mailSession.getTransport();
            transport.connect(user, password);
            
            if (selectedTechnique == 1) {
                String linkPart1 = "<img src='http://www.google-analytics.com/collect?v=1&tid=UA-00000000-1&cid=";
                String linkPart2 = "&t=event&ec=email&ea=open&cs=newsletter&cm=email&cn=NDS' width=1, height=1, style='display:none;'";
                String link;

                for (Map.Entry<String, String> s : map.entrySet()) {
                    message = new MimeMessage(mailSession);
                    message.setFrom(new InternetAddress(user));
                    message.setSubject(notificationSubject.getText());
                    link = linkPart1.concat(s.getKey()).concat("&el=").concat(s.getKey()).concat(linkPart2);
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(s.getValue()));
                    message.setText(notificationMessage.concat("<br>").concat(link), "utf-8", "html");
                    transport.sendMessage(message, message.getAllRecipients());
                }
            } else {
                String clickTrackingLink1 = "<a href=\"http://www.somewebsite.com/\" onclick=\"ga('send', 'event', 'Link', 'Click', '";
                String clickTrackingLink2 = "', 0)\">Link</a>";
                String clickTracking;
                
                for (Map.Entry<String, String> s : map.entrySet()) {
                    message = new MimeMessage(mailSession);
                    message.setFrom(new InternetAddress(user));
                    message.setSubject(notificationSubject.getText());
                    clickTracking = clickTrackingLink1.concat(s.getKey()).concat(clickTrackingLink2);
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(s.getValue()));
                    message.setText(notificationMessage.concat("<br>").concat(clickTracking), "utf-8", "html");
                    transport.sendMessage(message, message.getAllRecipients());
                }
            }
            
            transport.close();
            map.clear();
            
            for (String s : address) {
                String action = "INSERT INTO HISTORY (Recipient, Subject) VALUES ("
                        + "'" + s + "',"
                        + "'" + notificationSubject.getText() + "')";
                handler.execAction(action);
            }
            
            handler.execAction("UPDATE STATISTICS SET sent_counter = sent_counter + " + address.size() + "");
            
            clearFields();
            alertMessageLoad(AlertType.INFORMATION, "Notifications were successfully sent");
            
            initGraph();
            address.clear();
            clearEmailSearch();
        } else {
            alertMessageLoad(AlertType.ERROR, "Please fill in all fields");
        }
    }
    
    @FXML
    private void clearField(ActionEvent event) {
        toWhomSend.setText("");
    }
    
    private void clearFields() {
        toWhomSend.clear();
        toWhomSendList.clear();
        notificationSubject.clear();
        notificationText.clear();
    }
    
    private void radioBtnGroup() {
        ToggleGroup toggleGroup = new ToggleGroup();
        webbeaconRadioBtn.setToggleGroup(toggleGroup);
        clicktrackingRadioBtn.setToggleGroup(toggleGroup);
        toggleGroup.selectToggle(webbeaconRadioBtn);
    }
    
    @FXML
    private void webBeaconSelected(ActionEvent event) {
        selectedTechnique = 1;
    }

    @FXML
    private void clickTrackingSelected(ActionEvent event) {
        selectedTechnique = 2;
    }
    
    
    public void fillingToWhomField(boolean whatTextToAdd, int selected) {
        if (selected == 1) {
            
            if (toWhomSendList.getText().isEmpty()) {
                setToWhomSend("Selected Categories");
            } else {
                if (!toWhomSendList.getText().contains("Categories")) {
                    setToWhomSend(toWhomSendList.getText().concat(", Selected Categories"));
                }
            }
            
        } else {
            if (toWhomSendList.getText().isEmpty()) {
                setToWhomSend("Selected Customers");
            } else {
                if (!toWhomSendList.getText().contains("Customers")) {
                    setToWhomSend(toWhomSendList.getText().concat(", Selected Customers"));
                }
            }
        }
    }
    
    private LinkedHashSet<String> listOfRecipients() {
        LinkedHashSet<String> recipients = new LinkedHashSet<>();
        
        if (!selectedCustomers.isEmpty()) {
            String[] customers = selectedCustomers.substring(4, selectedCustomers.length()).split(",");
            selectedCustomers = "";
            ResultSet rs;
            for (int i = 0; i < customers.length; i++) {
                rs = handler.execQuery("SELECT Email FROM CUSTOMERS WHERE ID = " + Integer.parseInt(customers[i]) + "");
                try {
                    while (rs.next()) {
                        recipients.add(rs.getString("Email"));
                    }
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (!selectedCategories.isEmpty()) {
            String[] categories = selectedCategories.substring(4, selectedCategories.length()).split(",");
            selectedCategories = "";
            ResultSet rs;
            for (int i = 0; i < categories.length; i++) {
                rs = handler.execQuery("SELECT Email FROM CUSTOMERS WHERE Category LIKE '%" + categories[i] + "%'");
                try {
                    while (rs.next()) {
                        recipients.add(rs.getString("Email"));
                    }
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (toWhomSend.getText().contains("@")) {
            String[] array = toWhomSend.getText().split(",");
            for (int i = 0; i < array.length; i++) {
                if (array[i].contains("@") && array[i].contains(".")) {
                    recipients.add(array[i].trim());
                }
            }
        }
        
        return recipients;
    }
    
    
    public static void setToWhomSend(String toWhom) {
        Controller.recipientsList = toWhom;
        toWhomSendList.setText(recipientsList);
    }

    
    public void setSelectedCategories(String selectedCategories) {
        Controller.selectedCategories = selectedCategories;
    }
    
    public void setSelectedCustomers(String selectedCustomers) {
        Controller.selectedCustomers = selectedCustomers;
    }
    
    
    /*Statistics section*/
    
    @FXML
    private void resetStatistics(ActionEvent event) {
        Alert alert;
        if (emailInfoTitle.getText().equals("The Found Information")) {
            clearEmailSearch();
        }
        
        alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset statistics?",
                ButtonType.YES, ButtonType.CANCEL);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {

            if (handler.execAction("UPDATE STATISTICS SET sent_counter = 0, "
                    + "sent_opened = " + Analytics.getEventsNumber())) {
                alertMessageLoad(AlertType.INFORMATION, "Statistics have been reset");
                refreshGraph();
                try {
                    clearTheFile();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    PrintWriter writer = new PrintWriter(file);
                    writer.write(dateFormat.format(date).toString());
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                alertMessageLoad(AlertType.ERROR, "Oops, some error, try again");
            }
        }
    }
    
    private void clearTheFile() throws IOException {
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.flush();
    }

    @FXML
    private void clearEmailHistory(ActionEvent event) {
        Alert alert;

        alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear email history?",
                ButtonType.YES, ButtonType.CANCEL);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString()));
        alert.setHeaderText(null);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {

            if (lastSentEmail.getOpacity() == 1) {
                lastSentEmail.setOpacity(0);
                buttonNextEmail.setOpacity(0);
                buttonPreviousEmail.setOpacity(0);
                buttonNextEmail.setDisable(true);
                buttonPreviousEmail.setDisable(true);
                searchEmail.clear();

                if (handler.execAction("TRUNCATE TABLE HISTORY")) {
                    try {
                        handler.execUpdate("ALTER TABLE HISTORY ALTER COLUMN ID RESTART WITH 1");
                    } catch (SQLException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    alertMessageLoad(AlertType.INFORMATION, "The email history has been cleared");
                } else {
                    searchEmail.clear();
                    alertMessageLoad(AlertType.ERROR, "The email history is already cleared");
                }
            }
        }
    }
    
    private void lastSentEmailInfo() {
        ResultSet rs = handler.execQuery("SELECT ID FROM HISTORY");
        try {
            if (rs.next() != false) {
                if (rs != null) {
                    rs.close();
                }
                
                rs = handler.execQuery("SELECT Recipient, Subject, Date FROM HISTORY ORDER BY ID DESC FETCH FIRST ROW ONLY");
                while (rs.next()) {
                    lastRecipient.setText(rs.getString("Recipient"));
                    lastSubject.setText(rs.getString("Subject"));
                    lastDate.setText(rs.getTimestamp("Date").toLocaleString());
                }   
                if (rs != null) {
                    rs.close();
                }
                emailInfoTitle.setText("The Last Sent Email");
                lastSentEmail.setOpacity(1);   
            }
        } catch (SQLException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void searchEmailInfo(ActionEvent event) {
        boolean flag = false;
        clearEmailSearch();
        
        if (!searchEmail.getText().isEmpty()) {
            String searchInput = searchEmail.getText();
            emails = new ArrayList<>();
            Emails sentEmail;
            emailNumber = 0;
            
            if (!searchInput.matches(".*[a-z].*") && (searchInput.indexOf('-') == 4)
                    && (searchInput.indexOf('-', searchInput.indexOf('-') + 1) == 7)) {
                                
                ResultSet rs = handler.execQuery("SELECT Recipient, Subject, Date FROM HISTORY");
                try {
                    while (rs.next()) {
                        if (rs.getTimestamp("Date").toLocalDateTime().toString().substring(0, 10).equals(searchInput)) {
                            sentEmail = new Emails();
                            sentEmail.setRecipient(rs.getString("Recipient"));
                            sentEmail.setSubject(rs.getString("Subject"));
                            sentEmail.setDate(rs.getTimestamp("Date"));
                            emails.add(sentEmail);
                        }
                    }
                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                
            } else {
                
                if (searchInput.contains("@") && (searchInput.contains(".")) && (!searchInput.contains(" "))) {
                    ResultSet rs = handler.execQuery("SELECT Recipient, Subject, Date FROM HISTORY WHERE "
                            + "Recipient = '" + searchInput + "'");
                    try {
                        while (rs.next()) {
                            sentEmail = new Emails();
                            sentEmail.setRecipient(rs.getString("Recipient"));
                            sentEmail.setSubject(rs.getString("Subject"));
                            sentEmail.setDate(rs.getTimestamp("Date"));
                            emails.add(sentEmail);
                        }
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                } else {
                    
                    ResultSet rs = handler.execQuery("SELECT Recipient, Subject, Date FROM HISTORY WHERE "
                            + "Subject = '" + searchInput + "'");
                    try {
                        while (rs.next()) {
                            sentEmail = new Emails();
                            sentEmail.setRecipient(rs.getString("Recipient"));
                            sentEmail.setSubject(rs.getString("Subject"));
                            sentEmail.setDate(rs.getTimestamp("Date"));
                            emails.add(sentEmail);
                        }
                        if (rs != null) {
                            rs.close();
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }   
            }
            
            
            if (!emails.isEmpty()) {
                emailInfoTitle.setText("The Found Information");
                flag = true;
                if (emails.size() == 1) {
                    setEmailInfo(emails.get(0).getRecipient(), emails.get(0).getSubject(),
                            emails.get(0).getDate().toLocaleString());
                    emails.clear();
                } else {
                    setEmailInfo(emails.get(0).getRecipient(), emails.get(0).getSubject(),
                            emails.get(0).getDate().toLocaleString());
                    buttonNextEmail.setOpacity(1);
                    buttonNextEmail.setDisable(false);
                    buttonPreviousEmail.setDisable(true);
                }
            }
        }
        
        if (!flag) {
            alertMessageLoad(AlertType.ERROR, "Nothing found");
        }
    }
    
    private void setEmailInfo(String r, String s, String d) {
        lastRecipient.setText(r);
        lastSubject.setText(s);
        lastDate.setText(d);
    }

    @FXML
    private void nextEmailInfo(ActionEvent event) {
        emailNumber++;
        changerOfEmailInfo(emailNumber);
    }

    @FXML
    private void previousEmailInfo(ActionEvent event) {
        emailNumber--;
        changerOfEmailInfo(emailNumber);
    }
    
    private void changerOfEmailInfo(int number) {
        if (number == (emails.size() - 1)) {
            buttonNextEmail.setOpacity(0);
            buttonNextEmail.setDisable(true);
        } else {
            buttonNextEmail.setOpacity(1);
            buttonNextEmail.setDisable(false);
        }

        if (number == 0) {
            buttonPreviousEmail.setOpacity(0);
            buttonPreviousEmail.setDisable(true);
        } else {
            buttonPreviousEmail.setOpacity(1);
            buttonPreviousEmail.setDisable(false);
        }

        setEmailInfo(emails.get(number).getRecipient(), emails.get(number).getSubject(),
                emails.get(number).getDate().toLocaleString());
        
    }
    
    private void clearEmailSearch() {
        lastSentEmailInfo();
        buttonNextEmail.setOpacity(0);
        buttonPreviousEmail.setOpacity(0);
        buttonNextEmail.setDisable(true);
        buttonPreviousEmail.setDisable(true);
        if (emails != null) {
            emails.clear();
        }
    }

    private void initGraph() {
        if (handler.getStatistics() == null) {
            chartImage.setOpacity(1);
            chartLabel.setOpacity(1);
        } else {
            statisticsChart.setData(handler.getStatistics());
            statisticsTab.setOnSelectionChanged((event) -> {
                if (statisticsTab.isSelected()) {
                    refreshGraph();
                }
            });
        }
    }
    
    private void refreshGraph() {
        if (handler.getStatistics() == null) {
            statisticsChart.setOpacity(0);
            chartImage.setOpacity(1);
            chartLabel.setOpacity(1);
        } else {
            statisticsChart.setOpacity(1);
            chartImage.setOpacity(0);
            chartLabel.setOpacity(0);
            statisticsChart.setData(handler.getStatistics());
        }
    }

}