package org.example.budgetmanagementapp.service;

import org.example.budgetmanagementapp.domain.Plata;
import org.example.budgetmanagementapp.repository.PlataRepository;

import java.util.List;

public class PlataService {
    private final PlataRepository plataRepository;

    public PlataService(PlataRepository plataRepository) {
        this.plataRepository = plataRepository;
    }

    public List<Plata> getPlatiForUser(Long userId) {
        return plataRepository.findAllByUserId(userId);
    }

    public Plata addPlata(Plata plata) {
        return plataRepository.save(plata);
    }

    public Plata updatePlata(Plata plata) {
        return plataRepository.update(plata);
    }

    public void deletePlata(Long id) {
        plataRepository.delete(id);
    }

    public List<Plata> importPlati(List<Plata> plati) {
        return plataRepository.saveAll(plati);
    }
}
