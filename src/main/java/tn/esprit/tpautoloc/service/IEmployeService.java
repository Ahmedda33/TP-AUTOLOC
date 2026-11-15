package tn.esprit.tpautoloc.service;

import java.util.List;
import tn.esprit.tpautoloc.domain.Employe;

public interface IEmployeService {

    Employe create(Employe employe);

    Employe findById(Long id);

    List<Employe> findAll();

    Employe update(Long id, Employe employe);

    void deleteById(Long id);

    long count();

    boolean existsById(Long id);

    /** Rattache l'employé à l'agence (transfert si l'employé appartenait déjà à une autre agence). */
    void affecterEmployeAAgence(Long employeId, Long agenceId);

    /** Détache l'employé de son agence ; refusé (IllegalArgumentException) s'il n'en a aucune. */
    void desaffecterEmployeDeAgence(Long employeId);
}
