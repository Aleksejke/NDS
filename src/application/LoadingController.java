package application;

import analytics.Analytics;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LoadingController implements Initializable {

    @FXML
    private AnchorPane bgPane;

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new loadingWindow().start();
    }

    class loadingWindow extends Thread {

        @Override
        public void run() {
            try {
                AnalyticsReporting service = Analytics.initializeAnalyticsReporting();
                GetReportsResponse response = Analytics.getReport(service);
                Analytics.printResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Parent root = null;
                    try {
                        root = FXMLLoader.load(getClass().getResource("interface.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Stage stage = new Stage();
                    stage.setTitle("");
                    stage.initStyle(StageStyle.UNDECORATED);

                    //moving a stage
                    root.setOnMousePressed(event -> {
                        xOffset = event.getSceneX();
                        yOffset = event.getSceneY();
                    });
                    root.setOnMouseDragged(event -> {
                        stage.setX(event.getScreenX() - xOffset);
                        stage.setY(event.getScreenY() - yOffset);
                    });

                    stage.setScene(new Scene(root, 960, 616));
                    stage.show();

                    //hiding loading window
                    Stage st = (Stage) bgPane.getScene().getWindow();
                    st.close();
                }
            });
        }
    }

}
