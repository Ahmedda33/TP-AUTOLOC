package tn.esprit.tpautoloc.service;

import java.util.List;
import tn.esprit.tpautoloc.web.dto.PaiementDTO;

/**
 * Lecture seule : les paiements sont créés, modifiés et supprimés via le Contrat.
 */
public interface IPaiementService {

    PaiementDTO findById(Long id);

    List<PaiementDTO> findAll();

    long count();

    boolean existsById(Long id);
}
