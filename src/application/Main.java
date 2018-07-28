package application;

import database.DatabaseHandler;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loading.fxml"));
        primaryStage.setScene(new Scene(root, 400, 250));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
        
        new Thread(() -> {
            DatabaseHandler.getInstance();
        }).start();
        
    }


    public static void main(String[] args) {
        launch(args);
    }
}
