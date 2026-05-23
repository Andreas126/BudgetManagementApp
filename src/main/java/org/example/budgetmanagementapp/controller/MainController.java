package org.example.budgetmanagementapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.repository.JdbcCheltuialaRecurentaRepository;
import org.example.budgetmanagementapp.repository.JdbcPlataRepository;
import org.example.budgetmanagementapp.repository.JdbcUserRepository;
import org.example.budgetmanagementapp.repository.PlataRepository;
import org.example.budgetmanagementapp.service.CheltuialaRecurentaService;
import org.example.budgetmanagementapp.service.PlataService;
import org.example.budgetmanagementapp.service.UserService;

import java.io.IOException;

public class MainController {

    @FXML private Label userLabel;
    @FXML private VBox contentArea;
    @FXML private Button btnTranzactii;
    @FXML private Button btnBuget;

    private User currentUser;
    private final UserService userService;
    private final PlataService plataService;
    private final CheltuialaRecurentaService recurentaService;

    public MainController() {
        this.userService = new UserService(new JdbcUserRepository());
        PlataRepository plataRepository = new JdbcPlataRepository();
        this.plataService = new PlataService(plataRepository);
        this.recurentaService = new CheltuialaRecurentaService(
            new JdbcCheltuialaRecurentaRepository(),
            this.plataService
        );
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Utilizator: " + user.getUsername());

        // Procesare automata cheltuieli recurente la logare/startup
        try {
            recurentaService.proceseazaRecurente(user);
        } catch (Exception e) {
            System.err.println("Eroare la procesarea automata a recurentelor: " + e.getMessage());
        }

        // Incarcam pagina implicita (Tranzactii)
        showTranzactiiView();
    }

    @FXML
    public void showTranzactiiView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/budgetmanagementapp/tranzactii-view.fxml"));
            Parent view = loader.load();

            PlataController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Actualizare stil butoane navigare
            btnTranzactii.getStyleClass().add("nav-active");
            btnBuget.getStyleClass().remove("nav-active");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", "Nu s-a putut incarca pagina de tranzactii: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showBugetView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/budgetmanagementapp/buget-view.fxml"));
            Parent view = loader.load();

            BugetController controller = loader.getController();
            controller.setUser(currentUser, userService, plataService, recurentaService);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Actualizare stil butoane navigare
            btnBuget.getStyleClass().add("nav-active");
            btnTranzactii.getStyleClass().remove("nav-active");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", "Nu s-a putut incarca pagina de buget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/budgetmanagementapp/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 450, 450);
            scene.getStylesheets().add(
                getClass().getResource("/org/example/budgetmanagementapp/styles.css").toExternalForm());

            Stage loginStage = new Stage();
            loginStage.setTitle("Budget Insight - Autentificare");
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();

            // Inchidem fereastra curenta
            Stage currentStage = (Stage) contentArea.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", "Eroare la deconectare: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
