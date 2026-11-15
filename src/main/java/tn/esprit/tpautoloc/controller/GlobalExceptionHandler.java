package tn.esprit.tpautoloc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.tpautoloc.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduit les exceptions de la couche Service en réponses HTTP lisibles :
 * ResourceNotFoundException -> 404, IllegalArgumentException -> 400,
 * échec de Bean Validation (@Valid) -> 400 avec une map champ -> message (Atelier 7, Partie 5).
 * Les contrôleurs ne produisent jamais ces codes eux-mêmes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Corps JSON renvoyé en cas d'erreur : statut, message, horodatage. */
    public record ErreurReponse(int statut, String message, LocalDateTime horodatage) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErreurReponse> handleNotFound(ResourceNotFoundException ex) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErreurReponse> handleBadRequest(IllegalArgumentException ex) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** JSON mal formé, date hors format AAAA-MM-JJ ou valeur d'énumération invalide. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurReponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Corps de la requête illisible ou mal formé (JSON, date AAAA-MM-JJ ou énumération invalide)");
    }

    /** Échec de Bean Validation sur un DTO annoté @Valid : renvoie { champ : message }. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(erreur -> erreurs.put(erreur.getField(), erreur.getDefaultMessage()));
        return ResponseEntity.badRequest().body(erreurs);
    }

    private ResponseEntity<ErreurReponse> reponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErreurReponse(status.value(), message, LocalDateTime.now()));
    }
}
