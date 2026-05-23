package org.example.budgetmanagementapp.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.budgetmanagementapp.domain.Category;
import org.example.budgetmanagementapp.domain.CheltuialaRecurenta;
import org.example.budgetmanagementapp.domain.Plata;
import org.example.budgetmanagementapp.domain.TipPlata;
import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.service.CheltuialaRecurentaService;
import org.example.budgetmanagementapp.service.PlataService;
import org.example.budgetmanagementapp.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class BugetController {

    @FXML private TextField venitField;
    @FXML private Label cheltuitLabel;
    @FXML private Label ramasiLabel;
    @FXML private VBox remainingCard;

    @FXML private TableView<CheltuialaRecurenta> recurenteTable;
    @FXML private TableColumn<CheltuialaRecurenta, String> colId;
    @FXML private TableColumn<CheltuialaRecurenta, String> colSuma;
    @FXML private TableColumn<CheltuialaRecurenta, String> colTip;
    @FXML private TableColumn<CheltuialaRecurenta, String> colBeneficiar;
    @FXML private TableColumn<CheltuialaRecurenta, String> colCategorie;
    @FXML private TableColumn<CheltuialaRecurenta, String> colZi;
    @FXML private TableColumn<CheltuialaRecurenta, String> colUltimaProcesare;

    private User currentUser;
    private UserService userService;
    private PlataService plataService;
    private CheltuialaRecurentaService recurentaService;

    private final ObservableList<CheltuialaRecurenta> recurenteData = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        // Configurare coloane tabel
        colId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colSuma.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getSuma().toPlainString()));
        colTip.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getTip().name()));
        colBeneficiar.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getBeneficiar()));
        colCategorie.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getCategorie().name()));
        colZi.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getZiLuna())));
        colUltimaProcesare.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getUltimaProcesare() != null ?
                data.getValue().getUltimaProcesare().format(DATE_FORMAT) : "Niciodata"));

        recurenteTable.setItems(recurenteData);
        recurenteTable.setPlaceholder(new Label("Nu exista cheltuieli recurente setate"));
    }

    public void setUser(User user, UserService userService, PlataService plataService, CheltuialaRecurentaService recurentaService) {
        this.currentUser = user;
        this.userService = userService;
        this.plataService = plataService;
        this.recurentaService = recurentaService;

        venitField.setText(user.getVenitLunar() != null ? user.getVenitLunar().toPlainString() : "0.00");
        refreshBudgetInfo();
        refreshTable();
    }

    private void refreshTable() {
        recurenteData.clear();
        List<CheltuialaRecurenta> recurente = recurentaService.getRecurenteForUser(currentUser.getId());
        recurenteData.addAll(recurente);
    }

    private void refreshBudgetInfo() {
        BigDecimal venit = currentUser.getVenitLunar() != null ? currentUser.getVenitLunar() : BigDecimal.ZERO;

        // Calcul total cheltuit in luna curenta
        LocalDate astazi = LocalDate.now();
        List<Plata> plati = plataService.getPlatiForUser(currentUser.getId());
        BigDecimal cheltuit = plati.stream()
            .filter(p -> p.getDataOra().getMonth() == astazi.getMonth() && p.getDataOra().getYear() == astazi.getYear())
            .map(Plata::getSuma)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ramasi = venit.subtract(cheltuit);

        cheltuitLabel.setText(cheltuit.toPlainString() + " RON");
        ramasiLabel.setText(ramasi.toPlainString() + " RON");

        // Setare culori in functie de bugetul ramas
        remainingCard.getStyleClass().removeAll("card-success", "card-danger");
        if (ramasi.compareTo(BigDecimal.ZERO) >= 0) {
            remainingCard.getStyleClass().add("card-success");
        } else {
            remainingCard.getStyleClass().add("card-danger");
        }
    }

    @FXML
    public void onSetVenit() {
        try {
            BigDecimal nouVenit = new BigDecimal(venitField.getText().trim().replace(",", "."));
            if (nouVenit.compareTo(BigDecimal.ZERO) < 0) {
                showAlert(Alert.AlertType.WARNING, "Validare", "Venitul nu poate fi negativ!");
                return;
            }

            userService.updateVenitLunar(currentUser.getId(), nouVenit);
            currentUser.setVenitLunar(nouVenit);
            refreshBudgetInfo();
            showAlert(Alert.AlertType.INFORMATION, "Succes", "Venitul lunar a fost actualizat!");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Eroare", "Suma introdusa nu este valida!");
        }
    }

    @FXML
    public void onAddRecurenta() {
        Dialog<CheltuialaRecurenta> dialog = new Dialog<>();
        dialog.setTitle("Adaugare cheltuiala recurenta");
        dialog.setHeaderText("Introduceti datele cheltuielii recurente");

        ButtonType saveButtonType = new ButtonType("Salveaza", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField sumaField = new TextField();
        sumaField.setPromptText("ex: 150.50");

        ComboBox<TipPlata> tipCombo = new ComboBox<>();
        tipCombo.getItems().addAll(TipPlata.values());
        tipCombo.setValue(TipPlata.CARD);

        TextField beneficiarField = new TextField();
        beneficiarField.setPromptText("ex: Chirie");

        ComboBox<Category> categorieCombo = new ComboBox<>();
        categorieCombo.getItems().addAll(Category.values());
        categorieCombo.setValue(Category.OTHERS);

        TextField ziField = new TextField();
        ziField.setPromptText("ex: 5");

        grid.add(new Label("Suma (RON):"), 0, 0);
        grid.add(sumaField, 1, 0);
        grid.add(new Label("Tip plata:"), 0, 1);
        grid.add(tipCombo, 1, 1);
        grid.add(new Label("Beneficiar:"), 0, 2);
        grid.add(beneficiarField, 1, 2);
        grid.add(new Label("Categorie:"), 0, 3);
        grid.add(categorieCombo, 1, 3);
        grid.add(new Label("Ziua din luna (1-31):"), 0, 4);
        grid.add(ziField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    BigDecimal suma = new BigDecimal(sumaField.getText().trim().replace(",", "."));
                    String beneficiar = beneficiarField.getText().trim();
                    TipPlata tip = tipCombo.getValue();
                    Category categorie = categorieCombo.getValue();
                    int zi = Integer.parseInt(ziField.getText().trim());

                    if (beneficiar.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Validare", "Beneficiarul nu poate fi gol!");
                        return null;
                    }

                    if (zi < 1 || zi > 31) {
                        showAlert(Alert.AlertType.WARNING, "Validare", "Ziua trebuie sa fie intre 1 si 31!");
                        return null;
                    }

                    CheltuialaRecurenta recurenta = new CheltuialaRecurenta();
                    recurenta.setUserId(currentUser.getId());
                    recurenta.setSuma(suma);
                    recurenta.setTip(tip);
                    recurenta.setBeneficiar(beneficiar);
                    recurenta.setCategorie(categorie);
                    recurenta.setZiLuna(zi);
                    return recurenta;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Eroare", "Suma sau ziua din luna nu sunt valide!");
                }
            }
            return null;
        });

        Optional<CheltuialaRecurenta> result = dialog.showAndWait();
        result.ifPresent(recurenta -> {
            recurentaService.addRecurenta(recurenta);
            
            // Dupa adaugare, rulam procesarea pentru a inregistra plata automat daca ziua din luna curenta a trecut deja
            recurentaService.proceseazaRecurente(currentUser);
            
            refreshTable();
            refreshBudgetInfo();
        });
    }

    @FXML
    public void onDeleteRecurenta() {
        CheltuialaRecurenta selected = recurenteTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selectie", "Selectati o cheltuiala recurenta din tabel pentru a o sterge.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmare stergere");
        confirm.setHeaderText("Stergere cheltuiala recurenta");
        confirm.setContentText("Sigur doriti sa stergeti cheltuiala recurenta catre " + selected.getBeneficiar() +
            " in valoare de " + selected.getSuma() + " RON?");

        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            recurentaService.deleteRecurenta(selected.getId());
            refreshTable();
            refreshBudgetInfo();
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
