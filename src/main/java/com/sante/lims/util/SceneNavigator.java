package com.sante.lims.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {
    private static Stage primaryStage;

    private SceneNavigator() {
    }

    public static void initialize(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load scene: " + fxmlPath, e);
        }
    }
}
