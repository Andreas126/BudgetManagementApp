package org.example.budgetmanagementapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.budgetmanagementapp.domain.Category;
import org.example.budgetmanagementapp.domain.Plata;
import org.example.budgetmanagementapp.domain.TipPlata;
import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.repository.JdbcPlataRepository;
import org.example.budgetmanagementapp.service.CsvImportService;
import org.example.budgetmanagementapp.service.PlataService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class PlataController {

    @FXML private TableView<Plata> platiTable;
    @FXML private TableColumn<Plata, String> colId;
    @FXML private TableColumn<Plata, String> colSuma;
    @FXML private TableColumn<Plata, String> colDataOra;
    @FXML private TableColumn<Plata, String> colTip;
    @FXML private TableColumn<Plata, String> colBeneficiar;
    @FXML private TableColumn<Plata, String> colCategorie;
    @FXML private Label userLabel;

    private final PlataService plataService;
    private final CsvImportService csvImportService;
    private User currentUser;
    private final ObservableList<Plata> platiData = FXCollections.observableArrayList();

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public PlataController() {
        this.plataService = new PlataService(new JdbcPlataRepository());
        this.csvImportService = new CsvImportService();
    }

    @FXML
    public void initialize() {
        // Configurare coloane tabel
        colId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colSuma.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getSuma().toPlainString()));
        colDataOra.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDataOra().format(DISPLAY_FORMAT)));
        colTip.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTip().name()));
        colBeneficiar.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getBeneficiar()));
        colCategorie.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCategorie() != null ?
                data.getValue().getCategorie().name() : ""));

        platiTable.setItems(platiData);
        platiTable.setPlaceholder(new Label("Nu exista plati inregistrate"));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Utilizator: " + user.getUsername());
        refreshTable();
    }

    private void refreshTable() {
        platiData.clear();
        List<Plata> plati = plataService.getPlatiForUser(currentUser.getId());
        platiData.addAll(plati);
    }

    @FXML
    public void onAddPlata() {
        Optional<Plata> result = showPlataDialog(null);
        result.ifPresent(plata -> {
            plata.setUserId(currentUser.getId());
            plataService.addPlata(plata);
            refreshTable();
        });
    }

    @FXML
    public void onEditPlata() {
        Plata selected = platiTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selectie", "Selectati o plata din tabel pentru a o modifica.");
            return;
        }

        Optional<Plata> result = showPlataDialog(selected);
        result.ifPresent(plata -> {
            plata.setId(selected.getId());
            plata.setUserId(currentUser.getId());
            plataService.updatePlata(plata);
            refreshTable();
        });
    }

    @FXML
    public void onDeletePlata() {
        Plata selected = platiTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selectie", "Selectati o plata din tabel pentru a o sterge.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmare stergere");
        confirm.setHeaderText("Stergere plata");
        confirm.setContentText("Sigur doriti sa stergeti plata catre " + selected.getBeneficiar() +
            " in valoare de " + selected.getSuma() + " RON?");

        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            plataService.deletePlata(selected.getId());
            refreshTable();
        }
    }

    @FXML
    public void onImportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selectati fisierul CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fisiere CSV", "*.csv"));

        Stage stage = (Stage) platiTable.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                List<Plata> plati = csvImportService.parseCsvFile(file, currentUser.getId());
                if (plati.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Import CSV", "Nu s-au gasit plati valide in fisier.");
                    return;
                }

                plataService.importPlati(plati);
                refreshTable();
                showAlert(Alert.AlertType.INFORMATION, "Import CSV",
                    "Au fost importate " + plati.size() + " plati cu succes!");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Eroare import", "Eroare la citirea fisierului:\n" + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Eroare import", "Eroare la importul platilor:\n" + e.getMessage());
            }
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

            Stage currentStage = (Stage) platiTable.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", "Eroare la deconectare: " + e.getMessage());
        }
    }

    private Optional<Plata> showPlataDialog(Plata existing) {
        Dialog<Plata> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Adaugare plata" : "Modificare plata");
        dialog.setHeaderText(existing == null ? "Introduceti datele platii" : "Modificati datele platii");

        ButtonType saveButtonType = new ButtonType("Salveaza", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField sumaField = new TextField();
        sumaField.setPromptText("ex: 150.50");

        TextField dataField = new TextField();
        dataField.setPromptText("dd.MM.yyyy HH:mm");

        ComboBox<TipPlata> tipCombo = new ComboBox<>();
        tipCombo.getItems().addAll(TipPlata.values());

        TextField beneficiarField = new TextField();
        beneficiarField.setPromptText("ex: Mega Image");

        ComboBox<Category> categorieCombo = new ComboBox<>();
        categorieCombo.getItems().addAll(Category.values());

        // Pre-fill daca modificam
        if (existing != null) {
            sumaField.setText(existing.getSuma().toPlainString());
            dataField.setText(existing.getDataOra().format(DISPLAY_FORMAT));
            tipCombo.setValue(existing.getTip());
            beneficiarField.setText(existing.getBeneficiar());
            categorieCombo.setValue(existing.getCategorie());
        } else {
            dataField.setText(LocalDateTime.now().format(DISPLAY_FORMAT));
            tipCombo.setValue(TipPlata.CARD);
            categorieCombo.setValue(Category.OTHERS);
        }

        grid.add(new Label("Suma (RON):"), 0, 0);
        grid.add(sumaField, 1, 0);
        grid.add(new Label("Data si ora:"), 0, 1);
        grid.add(dataField, 1, 1);
        grid.add(new Label("Tip plata:"), 0, 2);
        grid.add(tipCombo, 1, 2);
        grid.add(new Label("Beneficiar:"), 0, 3);
        grid.add(beneficiarField, 1, 3);
        grid.add(new Label("Categorie:"), 0, 4);
        grid.add(categorieCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Convertim rezultatul dialogului
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    BigDecimal suma = new BigDecimal(sumaField.getText().trim().replace(",", "."));
                    LocalDateTime dataOra = LocalDateTime.parse(
                        dataField.getText().trim(),
                        DISPLAY_FORMAT);
                    TipPlata tip = tipCombo.getValue();
                    String beneficiar = beneficiarField.getText().trim();
                    Category categorie = categorieCombo.getValue();

                    if (beneficiar.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Validare", "Beneficiarul nu poate fi gol!");
                        return null;
                    }

                    Plata plata = new Plata();
                    plata.setSuma(suma);
                    plata.setDataOra(dataOra);
                    plata.setTip(tip);
                    plata.setBeneficiar(beneficiar);
                    plata.setCategorie(categorie);
                    return plata;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Eroare", "Suma introdusa nu este valida!");
                } catch (DateTimeParseException e) {
                    showAlert(Alert.AlertType.ERROR, "Eroare",
                        "Format de data invalid! Folositi: dd.MM.yyyy HH:mm");
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
