package tn.esprit.tpautoloc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.domain.Contrat;
import tn.esprit.tpautoloc.domain.Reservation;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.repository.IContratRepository;
import tn.esprit.tpautoloc.repository.IReservationRepository;
import tn.esprit.tpautoloc.service.IContratService;
import tn.esprit.tpautoloc.web.dto.ContratDTO;
import tn.esprit.tpautoloc.web.mapper.ContratMapper;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContratServiceImpl implements IContratService {

    private static final String RESSOURCE = "Contrat";

    private final IContratRepository contratRepository;
    private final IReservationRepository reservationRepository;
    private final ContratMapper contratMapper;

    @Override
    public ContratDTO create(ContratDTO dto) {
        if (dto.getId() != null) {
            throw new IllegalArgumentException("Un nouveau contrat ne doit pas avoir d'identifiant");
        }
        verifierMontant(dto);
        Contrat contrat = contratMapper.toEntity(dto);
        contrat.setReservation(chargerReservation(dto.getReservationId()));
        return contratMapper.toDto(contratRepository.save(contrat));
    }

    @Override
    @Transactional(readOnly = true)
    public ContratDTO findById(Long id) {
        return contratMapper.toDto(chargerContrat(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratDTO> findAll() {
        return contratRepository.findAll().stream().map(contratMapper::toDto).toList();
    }

    /** Recopie les attributs simples ; les paiements existants restent inchangés. */
    @Override
    public ContratDTO update(Long id, ContratDTO dto) {
        Contrat existant = chargerContrat(id);
        verifierMontant(dto);
        existant.setDateSignature(dto.getDateSignature());
        existant.setMontantTotal(dto.getMontantTotal());
        existant.setValide(dto.getValide());
        if (dto.getReservationId() != null) {
            existant.setReservation(chargerReservation(dto.getReservationId()));
        }
        return contratMapper.toDto(contratRepository.save(existant));
    }

    /** Supprime aussi les paiements du contrat (cascade ALL). */
    @Override
    public void deleteById(Long id) {
        contratRepository.delete(chargerContrat(id));
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return contratRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return contratRepository.existsById(id);
    }

    private Contrat chargerContrat(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private Reservation chargerReservation(Long reservationId) {
        if (reservationId == null) {
            return null;
        }
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation", reservationId));
    }

    private void verifierMontant(ContratDTO dto) {
        if (dto.getMontantTotal() != null && dto.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total du contrat ne peut pas être négatif");
        }
    }
}
