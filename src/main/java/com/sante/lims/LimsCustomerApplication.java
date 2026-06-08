package com.sante.lims;

import com.sante.lims.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class LimsCustomerApplication extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.initialize(stage);
        SceneNavigator.switchScene("/fxml/login.fxml", "Sante LIMS - Login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
