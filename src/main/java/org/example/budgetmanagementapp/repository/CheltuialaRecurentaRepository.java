package org.example.budgetmanagementapp.repository;

import org.example.budgetmanagementapp.domain.CheltuialaRecurenta;
import java.time.LocalDate;
import java.util.List;

public interface CheltuialaRecurentaRepository {
    List<CheltuialaRecurenta> findAllByUserId(Long userId);
    CheltuialaRecurenta save(CheltuialaRecurenta recurenta);
    void delete(Long id);
    void updateUltimaProcesare(Long id, LocalDate data);
}
