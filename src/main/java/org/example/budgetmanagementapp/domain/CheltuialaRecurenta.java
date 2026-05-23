package org.example.budgetmanagementapp.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CheltuialaRecurenta {
    private Long id;
    private Long userId;
    private BigDecimal suma;
    private TipPlata tip;
    private String beneficiar;
    private Category categorie;
    private int ziLuna;
    private LocalDate ultimaProcesare;

    public CheltuialaRecurenta() {}

    public CheltuialaRecurenta(Long userId, BigDecimal suma, TipPlata tip,
                               String beneficiar, Category categorie, int ziLuna,
                               LocalDate ultimaProcesare) {
        this.userId = userId;
        this.suma = suma;
        this.tip = tip;
        this.beneficiar = beneficiar;
        this.categorie = categorie;
        this.ziLuna = ziLuna;
        this.ultimaProcesare = ultimaProcesare;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getSuma() { return suma; }
    public void setSuma(BigDecimal suma) { this.suma = suma; }

    public TipPlata getTip() { return tip; }
    public void setTip(TipPlata tip) { this.tip = tip; }

    public String getBeneficiar() { return beneficiar; }
    public void setBeneficiar(String beneficiar) { this.beneficiar = beneficiar; }

    public Category getCategorie() { return categorie; }
    public void setCategorie(Category categorie) { this.categorie = categorie; }

    public int getZiLuna() { return ziLuna; }
    public void setZiLuna(int ziLuna) { this.ziLuna = ziLuna; }

    public LocalDate getUltimaProcesare() { return ultimaProcesare; }
    public void setUltimaProcesare(LocalDate ultimaProcesare) { this.ultimaProcesare = ultimaProcesare; }
}
