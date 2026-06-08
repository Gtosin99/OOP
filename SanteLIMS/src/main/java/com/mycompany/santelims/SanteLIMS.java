package com.mycompany.santelims;

import com.mycompany.santelims.utils.DBConnection;
import java.sql.Connection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SanteLIMS extends Application {

    @Override
    public void start(Stage stage) {

        try {
            System.out.println("APP STARTING...");

            // =========================
            // DATABASE TEST (NEW PART)
            // =========================
            System.out.println("Testing database connection...");

            Connection conn = DBConnection.getConnection();

            if (conn != null) {
                System.out.println("DB CONNECTED ✔");
            } else {
                System.out.println("DB FAILED ❌");
            }

            // =========================
            // JAVAFX LOADING
            // =========================
            var fxmlUrl = getClass().getResource("/login.fxml");

            System.out.println("FXML URL = " + fxmlUrl);

            if (fxmlUrl == null) {
                System.out.println("❌ login.fxml NOT FOUND - check src/main/resources/");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            Scene scene = new Scene(loader.load());

            stage.setTitle("Sante LIMS System");
            stage.setScene(scene);
            stage.show();

            System.out.println("UI LOADED SUCCESSFULLY ✔");

        } catch (Exception e) {
            System.out.println("❌ ERROR WHILE STARTING APP:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}