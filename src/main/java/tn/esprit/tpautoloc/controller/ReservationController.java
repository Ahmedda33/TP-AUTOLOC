package tn.esprit.tpautoloc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.tpautoloc.service.IReservationService;
import tn.esprit.tpautoloc.web.dto.ReservationRequestDTO;
import tn.esprit.tpautoloc.web.dto.ReservationResponseDTO;
import tn.esprit.tpautoloc.web.validator.ReservationRequestValidator;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "Gestion des réservations")
public class ReservationController {

    private final IReservationService reservationService;
    private final ReservationRequestValidator reservationRequestValidator;

    @Operation(summary = "Lister les réservations")
    @ApiResponse(responseCode = "200", description = "Liste les réservations (éventuellement vide)")
    @GetMapping
    public List<ReservationResponseDTO> findAll() {
        return reservationService.findAll();
    }

    @Operation(summary = "Consulter une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation trouvée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    })
    @GetMapping("/{id}")
    public ReservationResponseDTO findById(@Parameter(description = "Identifiant de la réservation") @PathVariable Long id) {
        return reservationService.findById(id);
    }

    @Operation(summary = "Créer une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réservation créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides (champ obligatoire manquant, dates incohérentes)"),
            @ApiResponse(responseCode = "404", description = "Client ou véhicule introuvable")
    })
    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ReservationRequestDTO dto, BindingResult result) {
        return enregistrer(dto, result, null);
    }

    @Operation(summary = "Modifier une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation modifiée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@Parameter(description = "Identifiant de la réservation") @PathVariable Long id,
                                         @Valid @RequestBody ReservationRequestDTO dto,
                                         BindingResult result) {
        return enregistrer(dto, result, id);
    }

    @Operation(summary = "Annuler une réservation (notifie les observateurs)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réservation annulée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    })
    @PutMapping("/{id}/annuler")
    public ReservationResponseDTO annuler(@Parameter(description = "Identifiant de la réservation") @PathVariable Long id) {
        return reservationService.annulerReservation(id);
    }

    @Operation(summary = "Supprimer une réservation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réservation supprimée"),
            @ApiResponse(responseCode = "404", description = "Réservation introuvable")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@Parameter(description = "Identifiant de la réservation") @PathVariable Long id) {
        reservationService.deleteById(id);
    }

    /** Bean Validation (@Valid) puis validateur personnalisé ; création si id == null, sinon modification. */
    private ResponseEntity<Object> enregistrer(ReservationRequestDTO dto, BindingResult result, Long id) {
        reservationRequestValidator.validate(dto, result);

        if (result.hasErrors()) {
            List<String> erreurs = result.getFieldErrors().stream()
                    .map(erreur -> erreur.getField() + " : " + erreur.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(erreurs);
        }

        if (id == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(dto));
        }
        return ResponseEntity.ok(reservationService.update(id, dto));
    }
}
