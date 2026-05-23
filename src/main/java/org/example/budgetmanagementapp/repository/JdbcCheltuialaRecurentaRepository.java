package org.example.budgetmanagementapp.repository;

import org.example.budgetmanagementapp.domain.Category;
import org.example.budgetmanagementapp.domain.CheltuialaRecurenta;
import org.example.budgetmanagementapp.domain.TipPlata;
import org.example.budgetmanagementapp.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcCheltuialaRecurentaRepository implements CheltuialaRecurentaRepository {

    public JdbcCheltuialaRecurentaRepository() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS cheltuieli_recurente (" +
                     "id BIGSERIAL PRIMARY KEY, " +
                     "user_id BIGINT NOT NULL, " +
                     "suma DECIMAL(15, 2) NOT NULL, " +
                     "tip VARCHAR(20) NOT NULL CHECK (tip IN ('CARD', 'CASH', 'TRANSFER')), " +
                     "beneficiar VARCHAR(255) NOT NULL, " +
                     "categorie VARCHAR(50) NOT NULL CHECK (categorie IN ('FOOD', 'ENTERTAINMENT', 'TRANSPORT', 'UTILITIES', 'HEALTH', 'OTHERS')), " +
                     "zi_luna INT NOT NULL CHECK (zi_luna >= 1 AND zi_luna <= 31), " +
                     "ultima_procesare DATE, " +
                     "CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la initializarea tabelei cheltuieli_recurente", e);
        }
    }

    @Override
    public List<CheltuialaRecurenta> findAllByUserId(Long userId) {
        List<CheltuialaRecurenta> recurente = new ArrayList<>();
        String sql = "SELECT id, user_id, suma, tip, beneficiar, categorie, zi_luna, ultima_procesare " +
                     "FROM cheltuieli_recurente WHERE user_id = ? ORDER BY zi_luna ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recurente.add(mapRowToRecurenta(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la citirea cheltuielilor recurente", e);
        }
        return recurente;
    }

    @Override
    public CheltuialaRecurenta save(CheltuialaRecurenta recurenta) {
        String sql = "INSERT INTO cheltuieli_recurente (user_id, suma, tip, beneficiar, categorie, zi_luna, ultima_procesare) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, recurenta.getUserId());
            stmt.setBigDecimal(2, recurenta.getSuma());
            stmt.setString(3, recurenta.getTip().name());
            stmt.setString(4, recurenta.getBeneficiar());
            stmt.setString(5, recurenta.getCategorie().name());
            stmt.setInt(6, recurenta.getZiLuna());
            if (recurenta.getUltimaProcesare() != null) {
                stmt.setDate(7, Date.valueOf(recurenta.getUltimaProcesare()));
            } else {
                stmt.setNull(7, Types.DATE);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                recurenta.setId(rs.getLong("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea cheltuielii recurente", e);
        }
        return recurenta;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM cheltuieli_recurente WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea cheltuielii recurente", e);
        }
    }

    @Override
    public void updateUltimaProcesare(Long id, LocalDate data) {
        String sql = "UPDATE cheltuieli_recurente SET ultima_procesare = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (data != null) {
                stmt.setDate(1, Date.valueOf(data));
            } else {
                stmt.setNull(1, Types.DATE);
            }
            stmt.setLong(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea datei de procesare", e);
        }
    }

    private CheltuialaRecurenta mapRowToRecurenta(ResultSet rs) throws SQLException {
        CheltuialaRecurenta recurenta = new CheltuialaRecurenta();
        recurenta.setId(rs.getLong("id"));
        recurenta.setUserId(rs.getLong("user_id"));
        recurenta.setSuma(rs.getBigDecimal("suma"));
        recurenta.setTip(TipPlata.valueOf(rs.getString("tip")));
        recurenta.setBeneficiar(rs.getString("beneficiar"));
        recurenta.setCategorie(Category.valueOf(rs.getString("categorie")));
        recurenta.setZiLuna(rs.getInt("zi_luna"));
        Date date = rs.getDate("ultima_procesare");
        if (date != null) {
            recurenta.setUltimaProcesare(date.toLocalDate());
        }
        return recurenta;
    }
}
