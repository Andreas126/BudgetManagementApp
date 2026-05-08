package org.example.budgetmanagementapp.service;

import org.example.budgetmanagementapp.domain.Category;
import org.example.budgetmanagementapp.domain.Plata;
import org.example.budgetmanagementapp.domain.TipPlata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class CsvImportService {

    // Formaturi de data acceptate
    private static final DateTimeFormatter[] DATE_FORMATS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    };

    /**
     * Importa platile dintr-un fisier CSV.
     * Format asteptat (cu header): data,suma,tip,beneficiar,categorie
     * Exemplu linie: 2026-05-08 10:30:00,50.00,CARD,Mega Image,FOOD
     *
     * Formate de data acceptate:
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd HH:mm
     * - dd/MM/yyyy HH:mm:ss
     * - dd/MM/yyyy HH:mm
     * - dd.MM.yyyy HH:mm:ss
     * - dd.MM.yyyy HH:mm
     */
    public List<Plata> parseCsvFile(File file, Long userId) throws IOException {
        List<Plata> plati = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            // Citim prima linie (header) si o ignoram
            String header = reader.readLine();
            lineNumber++;

            if (header == null) {
                throw new IOException("Fisierul CSV este gol");
            }

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Plata plata = parseLine(line, userId);
                    plati.add(plata);
                } catch (Exception e) {
                    errors.add("Linia " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty() && plati.isEmpty()) {
            throw new IOException("Nu s-a putut importa nicio plata:\n" + String.join("\n", errors));
        }

        if (!errors.isEmpty()) {
            System.out.println("Avertismente la import CSV:");
            errors.forEach(System.out::println);
        }

        return plati;
    }

    private Plata parseLine(String line, Long userId) {
        // Tratam cazul in care valorile pot contine virgule in ghilimele
        String[] parts = splitCsvLine(line);

        if (parts.length < 4) {
            throw new IllegalArgumentException("Numarul de coloane este insuficient (minim 4: data,suma,tip,beneficiar)");
        }

        String dataStr = parts[0].trim();
        String sumaStr = parts[1].trim();
        String tipStr = parts[2].trim().toUpperCase();
        String beneficiar = parts[3].trim();
        String categorieStr = parts.length > 4 ? parts[4].trim().toUpperCase() : "OTHERS";

        // Parsarea datei
        LocalDateTime dataOra = parseDate(dataStr);

        // Parsarea sumei
        BigDecimal suma;
        try {
            suma = new BigDecimal(sumaStr.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Suma invalida: " + sumaStr);
        }

        // Parsarea tipului
        TipPlata tip;
        try {
            tip = TipPlata.valueOf(tipStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tip plata invalid: " + tipStr + " (acceptate: CARD, CASH, TRANSFER)");
        }

        // Parsarea categoriei
        Category categorie;
        try {
            categorie = Category.valueOf(categorieStr);
        } catch (IllegalArgumentException e) {
            categorie = Category.OTHERS;
        }

        return new Plata(userId, suma, dataOra, tip, beneficiar, categorie);
    }

    private LocalDateTime parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Format de data nerecunoscut: " + dateStr +
            " (acceptate: yyyy-MM-dd HH:mm:ss, dd/MM/yyyy HH:mm, dd.MM.yyyy HH:mm)");
    }

    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }
}
