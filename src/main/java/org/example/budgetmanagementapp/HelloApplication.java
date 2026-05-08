package org.example.budgetmanagementapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 450, 450);
        scene.getStylesheets().add(
            HelloApplication.class.getResource("styles.css").toExternalForm());

        stage.setTitle("Budget Insight - Autentificare");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
