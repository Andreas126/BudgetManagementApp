module org.example.budgetmanagementapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.budgetmanagementapp to javafx.fxml;
    opens org.example.budgetmanagementapp.controller to javafx.fxml;
    opens org.example.budgetmanagementapp.domain to javafx.base;

    exports org.example.budgetmanagementapp;
    exports org.example.budgetmanagementapp.domain;
    exports org.example.budgetmanagementapp.controller;
}