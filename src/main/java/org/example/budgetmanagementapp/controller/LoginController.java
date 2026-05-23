package org.example.budgetmanagementapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.repository.JdbcUserRepository;
import org.example.budgetmanagementapp.service.UserService;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserService userService;

    public LoginController() {
        this.userService = new UserService(new JdbcUserRepository());
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Completati toate campurile!");
            return;
        }

        try {
            Optional<User> userOpt = userService.authenticate(username, password);
            if (userOpt.isPresent()) {
                openMainWindow(userOpt.get());
            } else {
                errorLabel.setText("Username sau parola incorecta!");
            }
        } catch (Exception e) {
            errorLabel.setText("Eroare la conectare: " + e.getMessage());
        }
    }

    @FXML
    public void onRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Completati toate campurile!");
            return;
        }

        if (password.length() < 4) {
            errorLabel.setText("Parola trebuie sa aiba minim 4 caractere!");
            return;
        }

        try {
            userService.register(username, password);
            errorLabel.setStyle("-fx-text-fill: #2ed573;");
            errorLabel.setText("Cont creat cu succes! Puteti face login.");
        } catch (RuntimeException e) {
            errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
            errorLabel.setText(e.getMessage());
        }
    }

    private void openMainWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/budgetmanagementapp/main-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(
                getClass().getResource("/org/example/budgetmanagementapp/styles.css").toExternalForm());

            MainController controller = loader.getController();
            controller.setCurrentUser(user);

            Stage mainStage = new Stage();
            mainStage.setTitle("Budget Insight - " + user.getUsername());
            mainStage.setScene(scene);
            mainStage.setMinWidth(800);
            mainStage.setMinHeight(500);
            mainStage.show();

            // Inchidem fereastra de login
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } catch (IOException e) {
            errorLabel.setText("Eroare la deschiderea ferestrei principale");
            e.printStackTrace();
        }
    }
}
