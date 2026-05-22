package org.example.budgetmanagementapp.repository;

import org.example.budgetmanagementapp.domain.Category;
import org.example.budgetmanagementapp.domain.Plata;
import org.example.budgetmanagementapp.domain.TipPlata;
import org.example.budgetmanagementapp.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcPlataRepository implements PlataRepository {

    @Override
    public List<Plata> findAllByUserId(Long userId) {
        List<Plata> plati = new ArrayList<>();
        String sql = "SELECT id, user_id, suma, data_ora, tip, beneficiar, categorie " +
                     "FROM plati WHERE user_id = ? ORDER BY data_ora DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                plati.add(mapRowToPlata(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la citirea platilor", e);
        }
        return plati;
    }

    @Override
    public Plata save(Plata plata) {
        String sql = "INSERT INTO plati (user_id, suma, data_ora, tip, beneficiar, categorie) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setPlataParams(stmt, plata);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                plata.setId(rs.getLong("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea platii", e);
        }
        return plata;
    }

    @Override
    public Plata update(Plata plata) {
        String sql = "UPDATE plati SET suma = ?, data_ora = ?, tip = ?, beneficiar = ?, categorie = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, plata.getSuma());
            stmt.setTimestamp(2, Timestamp.valueOf(plata.getDataOra()));
            stmt.setString(3, plata.getTip().name());
            stmt.setString(4, plata.getBeneficiar());
            stmt.setString(5, plata.getCategorie().name());
            stmt.setLong(6, plata.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea platii", e);
        }
        return plata;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM plati WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la stergerea platii", e);
        }
    }

    @Override
    public List<Plata> saveAll(List<Plata> plati) {
        String sql = "INSERT INTO plati (user_id, suma, data_ora, tip, beneficiar, categorie) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Plata plata : plati) {
                    setPlataParams(stmt, plata);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        plata.setId(rs.getLong("id"));
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la importul platilor", e);
        }
        return plati;
    }

    private void setPlataParams(PreparedStatement stmt, Plata plata) throws SQLException {
        stmt.setLong(1, plata.getUserId());
        stmt.setBigDecimal(2, plata.getSuma());
        stmt.setTimestamp(3, Timestamp.valueOf(plata.getDataOra()));
        stmt.setString(4, plata.getTip().name());
        stmt.setString(5, plata.getBeneficiar());
        stmt.setString(6, plata.getCategorie() != null ? plata.getCategorie().name() : Category.OTHERS.name());
    }

    private Plata mapRowToPlata(ResultSet rs) throws SQLException {
        Plata plata = new Plata();
        plata.setId(rs.getLong("id"));
        plata.setUserId(rs.getLong("user_id"));
        plata.setSuma(rs.getBigDecimal("suma"));
        plata.setDataOra(rs.getTimestamp("data_ora").toLocalDateTime());
        plata.setTip(TipPlata.valueOf(rs.getString("tip")));
        plata.setBeneficiar(rs.getString("beneficiar"));
        String cat = rs.getString("categorie");
        if (cat != null) {
            plata.setCategorie(Category.valueOf(cat));
        }
        return plata;
    }
}
