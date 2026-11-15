# Notes — Atelier 7 : affectation, DTO, MapStruct, SonarLint, validation

> Les sections « SonarLint » décrivent une **relecture manuelle** du code. L'analyse SonarQube for IDE n'a pas pu être lancée
> depuis cet environnement : à refaire dans IntelliJ (clic droit sur le package > *Analyze with SonarQube for IDE*) et à compléter
> avec vos captures.

## Partie 1 — Affectation / désaffectation

Le côté propriétaire de chaque association est celui qui porte la clé étrangère : `Vehicule.agence` (`id_agence`) et `Employe.agence`
(`id_agence`). `Agence.vehicules` et `Agence.employes` sont les côtés inverses (`mappedBy`).

| Opération | Service | Endpoint |
|---|---|---|
| Affecter un véhicule | `IVehiculeService.affecterVehiculeAAgence(vehiculeId, agenceId)` | `PUT /api/vehicules/{vehiculeId}/affecter/{agenceId}` → 204 |
| Désaffecter un véhicule | `desaffecterVehiculeDeAgence(vehiculeId)` | `PUT /api/vehicules/{vehiculeId}/desaffecter` → 204 |
| Affecter un employé | `IEmployeService.affecterEmployeAAgence(employeId, agenceId)` | `PUT /api/employes/{employeId}/affecter/{agenceId}` → 204 |
| Désaffecter un employé | `desaffecterEmployeDeAgence(employeId)` | `PUT /api/employes/{employeId}/desaffecter` → 204 |

Règles : véhicule/employé ou agence introuvable → `ResourceNotFoundException` (404), vérifiée **avant** toute modification.
Affecter un élément déjà rattaché à une autre agence = transfert (la clé étrangère est simplement remplacée).
**Comportement choisi pour la désaffectation d'un élément sans agence** : `IllegalArgumentException` (400) plutôt qu'un no-op silencieux,
pour signaler l'incohérence à l'appelant. Les méthodes sont `@Transactional` (au niveau de la classe) : une erreur annule tout.

Tests : `AffectationServiceTest` (affectation, id inexistant, transfert, désaffectation, mêmes cas pour un employé).

## Parties 2 et 3 — DTO, MapStruct, services et contrôleurs

- Packages `web.dto`, `web.mapper`, `web.validator` (comme l'énoncé §2.2, §2.5, §5.4). Les contrôleurs restent dans `controller`.
- `pom.xml` : dépendance `mapstruct` 1.5.5.Final ; `annotationProcessorPaths` = Lombok, **lombok-mapstruct-binding**, mapstruct-processor
  (Lombok doit passer avant MapStruct, sinon les getters/setters ne sont pas vus).
- Les identifiants des entités s'appellent `idAgence`, `idVehicule`… alors que les DTO exposent `id` : chaque mapper déclare donc
  `@Mapping(target = "id", source = "idXxx")` (et l'inverse pour `toEntity`).
- Les associations (`agenceId`, `clientId`, `vehiculeId`, `reservationId`) ne peuvent pas être résolues par MapStruct : le mapper les ignore
  et le **service** charge l'entité par son identifiant (404 si elle n'existe pas).
- `ContratDTO` contient en plus `paiements` (non demandé par l'énoncé) pour garder la création d'un contrat avec ses paiements (cascade) ;
  `ContratMapper` renseigne `paiement.contrat` dans un `@AfterMapping`.
- Services convertis : Agence, Client, Contrat, Paiement (lecture seule), Vehicule, Reservation. `Employe`, `Equipement` et `Maintenance`
  n'ont pas de DTO dans l'énoncé et gardent leurs entités.
- `ReservationServiceImpl` : `create(ReservationRequestDTO)` charge client et véhicule, passe le statut à `CONFIRMEE`, enregistre, puis
  `reservationSubject.notifierConfirmation(...)` (Observer, Atelier 6). `tarifEstime` est calculé via `TarificationStrategyFactory`
  (Strategy + Factory, Atelier 6). `annulerReservation(id)` est exposée par `PUT /api/reservations/{id}/annuler`.

## Partie 4 — SonarLint (relecture manuelle)

| Point | Règle / risque | Traitement |
|---|---|---|
| Entités JPA en `@RequestBody` / retour de contrôleur | S4684 : couplage API ↔ base, mass assignment | Résolu pour Agence, Client, Contrat, Paiement, Vehicule, Reservation. **Reste** : Employe, Equipement, Maintenance (pas de DTO dans l'énoncé). |
| Littéraux dupliqués (« Agence », « Véhicule »…) | S1192 | Constantes `RESSOURCE` dans les services. |
| `findById(...).orElseThrow(...)` répété dans chaque méthode | duplication | Méthodes privées `chargerXxx(id)`. |
| Collecteurs `stream().collect(Collectors.toList())` | S6204 | `Stream.toList()`. |
| Valeurs nulles (`agenceId`, `reservationId`, dates) | S2259 / NPE | Gardes explicites (`chargerAgence(null)` renvoie `null`, `estimerTarif` renvoie `null` si véhicule, tarif ou dates absents). |
| Lecture seule non déclarée | bonne pratique transactionnelle | `@Transactional(readOnly = true)` sur `findById`, `findAll`, `count`, `existsById`. |

À ne **pas** corriger mécaniquement : l'injection par constructeur (`@RequiredArgsConstructor`) est déjà conforme à S6813.

## Partie 5 — Validation

- `spring-boot-starter-validation` était déjà dans le `pom.xml`. Contraintes avec messages explicites sur : `AgenceDTO`, `ClientDTO`,
  `ContratDTO`, `PaiementDTO`, `ReservationRequestDTO`, `VehiculeDTO` (voir §5.2 de l'énoncé).
- `GlobalExceptionHandler` : ajout du handler `MethodArgumentNotValidException` → 400 avec `{ champ : message }`.
  **Écart par rapport à l'énoncé** : la classe est restée dans `controller` (elle y existait depuis l'Atelier 5). La déplacer dans
  `exception` demande de supprimer l'ancienne (deux `@RestControllerAdvice` du même nom feraient échouer le démarrage) : à faire dans IntelliJ
  avec *Refactor > Move* si vous voulez coller à l'énoncé.
- `ReservationRequestValidator` : début aujourd'hui ou futur ; fin **strictement** postérieure au début ; dates nulles ignorées
  (traitées par Bean Validation). Appelé explicitement dans `ReservationController` (création et modification) après `@Valid` + `BindingResult` ;
  les erreurs sont renvoyées en 400 sous forme `["champ : message", ...]`.
- Tests : `ValidationDtoTest` (email invalide, véhicule sans immatriculation, paiement montant ≤ 0, réservation sans dates / début passé / valide)
  et `ReservationRequestValidatorTest` (fin ≤ début, début passé, dates cohérentes, dates absentes).

## À mettre à jour ensuite

La collection Postman n'a pas été modifiée : les corps JSON changent (`id` au lieu de `idAgence`, `agenceId` pour un véhicule,
`clientId`/`vehiculeId` pour une réservation, plus de `{ "agence": { "idAgence": 1 } }` sur Agence/Vehicule/Reservation).
