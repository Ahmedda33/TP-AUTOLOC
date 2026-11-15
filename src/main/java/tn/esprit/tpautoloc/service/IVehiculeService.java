package tn.esprit.tpautoloc.service;

import java.util.List;
import tn.esprit.tpautoloc.web.dto.VehiculeDTO;

public interface IVehiculeService {

    VehiculeDTO create(VehiculeDTO dto);

    VehiculeDTO findById(Long id);

    List<VehiculeDTO> findAll();

    VehiculeDTO update(Long id, VehiculeDTO dto);

    void deleteById(Long id);

    long count();

    boolean existsById(Long id);

    /** Rattache le véhicule à l'agence (transfert si le véhicule appartenait déjà à une autre agence). */
    void affecterVehiculeAAgence(Long vehiculeId, Long agenceId);

    /** Détache le véhicule de son agence ; refusé (IllegalArgumentException) s'il n'en a aucune. */
    void desaffecterVehiculeDeAgence(Long vehiculeId);
}
