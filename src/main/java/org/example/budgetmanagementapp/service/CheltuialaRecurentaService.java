package org.example.budgetmanagementapp.service;

import org.example.budgetmanagementapp.domain.CheltuialaRecurenta;
import org.example.budgetmanagementapp.domain.Plata;
import org.example.budgetmanagementapp.domain.User;
import org.example.budgetmanagementapp.repository.CheltuialaRecurentaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CheltuialaRecurentaService {
    private final CheltuialaRecurentaRepository recurentaRepository;
    private final PlataService plataService;

    public CheltuialaRecurentaService(CheltuialaRecurentaRepository recurentaRepository, PlataService plataService) {
        this.recurentaRepository = recurentaRepository;
        this.plataService = plataService;
    }

    public List<CheltuialaRecurenta> getRecurenteForUser(Long userId) {
        return recurentaRepository.findAllByUserId(userId);
    }

    public CheltuialaRecurenta addRecurenta(CheltuialaRecurenta recurenta) {
        return recurentaRepository.save(recurenta);
    }

    public void deleteRecurenta(Long id) {
        recurentaRepository.delete(id);
    }

    /**
     * Proceseaza automat cheltuielile recurente pentru utilizator.
     * Daca o cheltuiala este programata pentru o zi din luna care a sosit deja,
     * si nu a fost inca procesata in aceasta luna, se adauga automat ca plata in baza de date.
     */
    public void proceseazaRecurente(User user) {
        LocalDate astazi = LocalDate.now();
        List<CheltuialaRecurenta> recurente = recurentaRepository.findAllByUserId(user.getId());

        for (CheltuialaRecurenta recurenta : recurente) {
            LocalDate ultima = recurenta.getUltimaProcesare();
            
            // Verificam daca nu a fost procesata deloc sau a fost procesata intr-o luna anterioara
            if (ultima == null || ultima.getMonth() != astazi.getMonth() || ultima.getYear() != astazi.getYear()) {
                // Verificam daca ziua programata a sosit in aceasta luna
                if (astazi.getDayOfMonth() >= recurenta.getZiLuna()) {
                    // Generam o plata noua
                    Plata plata = new Plata();
                    plata.setUserId(user.getId());
                    plata.setSuma(recurenta.getSuma());
                    // Seta data inregistrarii la ziua stabilita din luna curenta
                    plata.setDataOra(LocalDateTime.of(astazi.getYear(), astazi.getMonth(), recurenta.getZiLuna(), 9, 0));
                    plata.setTip(recurenta.getTip());
                    plata.setBeneficiar(recurenta.getBeneficiar() + " (Recurent)");
                    plata.setCategorie(recurenta.getCategorie());

                    plataService.addPlata(plata);

                    // Actualizam data ultimei procesari pentru a nu rula din nou luna aceasta
                    recurentaRepository.updateUltimaProcesare(recurenta.getId(), astazi);
                }
            }
        }
    }
}
