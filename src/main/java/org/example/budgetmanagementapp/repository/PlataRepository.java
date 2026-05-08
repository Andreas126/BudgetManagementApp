package org.example.budgetmanagementapp.repository;

import org.example.budgetmanagementapp.domain.Plata;

import java.util.List;

public interface PlataRepository {
    List<Plata> findAllByUserId(Long userId);
    Plata save(Plata plata);
    Plata update(Plata plata);
    void delete(Long id);
    List<Plata> saveAll(List<Plata> plati);
}
