package tn.esprit.tpautoloc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.tpautoloc.domain.Client;
import tn.esprit.tpautoloc.domain.Reservation;
import tn.esprit.tpautoloc.domain.Vehicule;
import tn.esprit.tpautoloc.domain.enums.StatutReservation;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;
import tn.esprit.tpautoloc.repository.IClientRepository;
import tn.esprit.tpautoloc.repository.IReservationRepository;
import tn.esprit.tpautoloc.repository.IVehiculeRepository;
import tn.esprit.tpautoloc.service.IReservationService;
import tn.esprit.tpautoloc.service.factory.TarificationStrategyFactory;
import tn.esprit.tpautoloc.service.observer.ReservationSubject;
import tn.esprit.tpautoloc.web.dto.ReservationRequestDTO;
import tn.esprit.tpautoloc.web.dto.ReservationResponseDTO;
import tn.esprit.tpautoloc.web.mapper.ReservationMapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Création et annulation passent par le patron Observer (ReservationSubject) ;
 * le tarif estimé est calculé par la stratégie choisie par TarificationStrategyFactory.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements IReservationService {

    private static final String RESSOURCE = "Réservation";

    private final IReservationRepository reservationRepository;
    private final IClientRepository clientRepository;
    private final IVehiculeRepository vehiculeRepository;
    private final ReservationMapper reservationMapper;
    private final ReservationSubject reservationSubject;
    private final TarificationStrategyFactory tarificationStrategyFactory;

    @Override
    public ReservationResponseDTO create(ReservationRequestDTO dto) {
        verifierDates(dto);
        Reservation reservation = reservationMapper.toEntity(dto);
        reservation.setClient(chargerClient(dto.getClientId()));
        reservation.setVehicule(chargerVehicule(dto.getVehiculeId()));
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation saved = reservationRepository.save(reservation);
        reservationSubject.notifierConfirmation(saved);
        return versReponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponseDTO findById(Long id) {
        return versReponse(chargerReservation(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findAll() {
        return reservationRepository.findAll().stream().map(this::versReponse).toList();
    }

    @Override
    public ReservationResponseDTO update(Long id, ReservationRequestDTO dto) {
        Reservation existante = chargerReservation(id);
        verifierDates(dto);
        existante.setDateDebut(dto.getDateDebut());
        existante.setDateFin(dto.getDateFin());
        existante.setClient(chargerClient(dto.getClientId()));
        existante.setVehicule(chargerVehicule(dto.getVehiculeId()));
        return versReponse(reservationRepository.save(existante));
    }

    @Override
    public void deleteById(Long id) {
        reservationRepository.delete(chargerReservation(id));
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return reservationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public ReservationResponseDTO annulerReservation(Long id) {
        Reservation reservation = chargerReservation(id);
        reservation.setStatut(StatutReservation.ANNULEE);
        Reservation saved = reservationRepository.save(reservation);
        reservationSubject.notifierAnnulation(saved);
        return versReponse(saved);
    }

    private ReservationResponseDTO versReponse(Reservation reservation) {
        ReservationResponseDTO reponse = reservationMapper.toDto(reservation);
        reponse.setTarifEstime(estimerTarif(reservation));
        return reponse;
    }

    private BigDecimal estimerTarif(Reservation reservation) {
        Vehicule vehicule = reservation.getVehicule();
        if (vehicule == null || vehicule.getTarifJournalier() == null
                || reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            return null;
        }
        return tarificationStrategyFactory.resoudre(reservation).calculerTarif(vehicule, reservation);
    }

    private Reservation chargerReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESSOURCE, id));
    }

    private Client chargerClient(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
    }

    private Vehicule chargerVehicule(Long vehiculeId) {
        return vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule", vehiculeId));
    }

    private void verifierDates(ReservationRequestDTO dto) {
        if (dto.getClientId() == null || dto.getVehiculeId() == null) {
            throw new IllegalArgumentException("Le client et le véhicule sont obligatoires");
        }
        if (dto.getDateDebut() == null || dto.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires");
        }
        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
    }
}
