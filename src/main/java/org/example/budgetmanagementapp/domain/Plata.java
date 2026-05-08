package org.example.budgetmanagementapp.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Plata {
    private Long id;
    private Long userId;
    private BigDecimal suma;
    private LocalDateTime dataOra;
    private TipPlata tip;
    private String beneficiar;
    private Category categorie;

    public Plata() {}

    public Plata(Long userId, BigDecimal suma, LocalDateTime dataOra,
                 TipPlata tip, String beneficiar, Category categorie) {
        this.userId = userId;
        this.suma = suma;
        this.dataOra = dataOra;
        this.tip = tip;
        this.beneficiar = beneficiar;
        this.categorie = categorie;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BigDecimal getSuma() { return suma; }
    public void setSuma(BigDecimal suma) { this.suma = suma; }

    public LocalDateTime getDataOra() { return dataOra; }
    public void setDataOra(LocalDateTime dataOra) { this.dataOra = dataOra; }

    public TipPlata getTip() { return tip; }
    public void setTip(TipPlata tip) { this.tip = tip; }

    public String getBeneficiar() { return beneficiar; }
    public void setBeneficiar(String beneficiar) { this.beneficiar = beneficiar; }

    public Category getCategorie() { return categorie; }
    public void setCategorie(Category categorie) { this.categorie = categorie; }
}
