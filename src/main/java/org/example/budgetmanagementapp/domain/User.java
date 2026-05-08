package org.example.budgetmanagementapp.domain;

import java.math.BigDecimal;

public class User {
    private Long id;
    private String username;
    private String password;
    private BigDecimal venitLunar;

    public User() {}

    public User(String username, String password, BigDecimal venitLunar) {
        this.username = username;
        this.password = password;
        this.venitLunar = venitLunar;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getVenitLunar() { return venitLunar; }
    public void setVenitLunar(BigDecimal venitLunar) { this.venitLunar = venitLunar; }
}
