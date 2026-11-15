# Notes — Atelier 5 : Spring MVC, Postman et SpringDoc

> Collection Postman : `docs/postman/AutoLoc.postman_collection.json` (environnement : `docs/postman/local.postman_environment.json`).
> Prérequis : MySQL démarré, application sur `http://localhost:8080`, préfixe `/api` défini par `server.servlet.context-path`.
> Les passages marqués **(à vérifier)** n'ont pas encore été constatés à l'exécution (Postman, Swagger UI, SonarQube for IDE) : à confirmer puis à compléter avec vos captures.

## A. Endpoints

Tous les contrôleurs sont dans `tn.esprit.tpautoloc.controller`, sans le préfixe `/api` (il vient du `context-path`), avec `@RequiredArgsConstructor` et un champ `final` typé par l'interface du service. Aucun accès au repository.

Codes d'erreur : **404** = `ResourceNotFoundException`, **400** = `IllegalArgumentException` (ou corps JSON illisible), tous deux traduits par `GlobalExceptionHandler`.

| Contrôleur | Verbe et route | Service appelé | Succès | Erreurs |
|---|---|---|---|---|
| AgenceController | GET /api/agences | `findAll()` | 200 | — |
| | GET /api/agences/{id} | `findById(id)` | 200 | 404 |
| | POST /api/agences | `create(agence)` | 201 | 400 (id fourni, `nom` vide) |
| | PUT /api/agences/{id} | `update(id, agence)` | 200 | 404, 400 |
| | DELETE /api/agences/{id} | `deleteById(id)` | 204 | 404 |
| EmployeController | GET /api/employes | `findAll()` | 200 | — |
| | GET /api/employes/{id} | `findById(id)` | 200 | 404 |
| | POST /api/employes | `create(employe)` | 201 | 400 (id fourni, `nom` ou `role` absent) |
| | PUT /api/employes/{id} | `update(id, employe)` | 200 | 404, 400 |
| | DELETE /api/employes/{id} | `deleteById(id)` | 204 | 404 |
| VehiculeController | GET /api/vehicules | `findAll()` | 200 | — |
| | GET /api/vehicules/{id} | `findById(id)` | 200 | 404 |
| | POST /api/vehicules | `create(vehicule)` | 201 | 400 (id fourni, `tarifJournalier` négatif) |
| | PUT /api/vehicules/{id} | `update(id, vehicule)` | 200 | 404, 400 |
| | DELETE /api/vehicules/{id} | `deleteById(id)` | 204 | 404 |
| EquipementController | GET /api/equipements | `findAll()` | 200 | — |
| | GET /api/equipements/{id} | `findById(id)` | 200 | 404 |
| | POST /api/equipements | `create(equipement)` | 201 | 400 (id fourni, `libelle` vide) |
| | PUT /api/equipements/{id} | `update(id, equipement)` | 200 | 404, 400 |
| | DELETE /api/equipements/{id} | `deleteById(id)` | 204 | 404 |
| ClientController | GET /api/clients | `findAll()` | 200 | — |
| | GET /api/clients/{id} | `findById(id)` | 200 | 404 |
| | POST /api/clients | `create(client)` | 201 | 400 (id fourni, `nom` ou `email` absent) |
| | PUT /api/clients/{id} | `update(id, client)` | 200 | 404, 400 |
| | DELETE /api/clients/{id} | `deleteById(id)` | 204 | 404 |
| ReservationController | GET /api/reservations | `findAll()` | 200 | — |
| | GET /api/reservations/{id} | `findById(id)` | 200 | 404 |
| | POST /api/reservations | `create(reservation)` | 201 | 400 (id fourni, `dateFin` < `dateDebut`) |
| | PUT /api/reservations/{id} | `update(id, reservation)` | 200 | 404, 400 |
| | DELETE /api/reservations/{id} | `deleteById(id)` | 204 | 404 |
| ContratController | GET /api/contrats | `findAll()` | 200 | — |
| | GET /api/contrats/{id} | `findById(id)` | 200 | 404 |
| | POST /api/contrats | `create(contrat)` | 201 | 400 (id fourni, `montantTotal` négatif) |
| | PUT /api/contrats/{id} | `update(id, contrat)` | 200 | 404, 400 |
| | DELETE /api/contrats/{id} | `deleteById(id)` | 204 | 404 |
| PaiementController (lecture seule) | GET /api/paiements | `findAll()` | 200 | — |
| | GET /api/paiements/{id} | `findById(id)` | 200 | 404 |
| | POST /api/paiements | aucun (non mappé) | — | **405** Method Not Allowed |
| MaintenanceController | GET /api/maintenances | `findAll()` | 200 | — |
| | GET /api/maintenances/{id} | `findById(id)` | 200 | 404 |
| | POST /api/maintenances | `create(maintenance)` | 201 | 400 (id fourni, `dateFin` < `dateDebut`) |
| | PUT /api/maintenances/{id} | `update(id, maintenance)` | 200 | 404, 400 |
| | DELETE /api/maintenances/{id} | `deleteById(id)` | 204 | 404 |

Particularités des corps JSON (les identifiants portent le nom exact du modèle) :
- Employé : `"agence": { "idAgence": 1 }`
- Véhicule et maintenance : `"agence": { "idAgence": 1 }` / `"vehicule": { "idVehicule": 1 }`
- Réservation : `"client": { "idClient": 1 }, "vehicule": { "idVehicule": 1 }`
- Contrat avec paiements : `"paiements": [ { "montant": 150.00, "datePaiement": "2026-11-02", "modePaiement": "CARTE" }, ... ]`

## B. Annotations Jackson

| Association | Annotation retenue | Justification |
|---|---|---|
| `Contrat.paiements` ↔ `Paiement.contrat` | `@JsonManagedReference("contrat-paiements")` sur `paiements`, `@JsonBackReference("contrat-paiements")` sur `Paiement.contrat` | Seule vraie composition (`cascade = ALL`) : l'enfant se crée dans le JSON de son parent. La liste est sérialisée, la référence arrière est omise (pas de boucle) et Jackson renseigne `paiement.contrat` à la désérialisation. |
| `Agence.employes` ↔ `Employe.agence` | `@JsonIgnore` sur `Agence.employes` | L'employé est créé seul et désigne son agence par `{ "idAgence": 1 }` : l'attribut propriétaire `Employe.agence` doit rester lisible et écrivable. |
| `Agence.vehicules` ↔ `Vehicule.agence` | `@JsonIgnore` sur `Agence.vehicules` | Même cas : le véhicule désigne son agence par identifiant. |
| `Client.reservations` ↔ `Reservation.client` | `@JsonIgnore` sur `Client.reservations` | La réservation désigne son client par identifiant. |
| `Vehicule.reservations` ↔ `Reservation.vehicule` | `@JsonIgnore` sur `Vehicule.reservations` | Idem pour le véhicule. |
| `Vehicule.maintenances` ↔ `Maintenance.vehicule` | `@JsonIgnore` sur `Vehicule.maintenances` | La maintenance désigne son véhicule par identifiant. |
| `Vehicule.vehiculeEquipements` ↔ `VehiculeEquipement.vehicule` | `@JsonIgnore` sur `Vehicule.vehiculeEquipements` | Table d'association non exposée par un contrôleur ; évite la boucle. |
| `Equipement.vehiculeEquipements` ↔ `VehiculeEquipement.equipement` | `@JsonIgnore` sur `Equipement.vehiculeEquipements` | Idem. |
| `Reservation.contrat` ↔ `Contrat.reservation` | `@JsonIgnore` sur `Reservation.contrat` (côté inverse, `mappedBy`) | L'attribut propriétaire est `Contrat.reservation` ; on coupe la boucle côté inverse. |

**`@JsonBackReference` ou `@JsonIgnore` ?** `@JsonBackReference` n'a de sens qu'avec son `@JsonManagedReference` (même nom) : elle omet la propriété à la **sérialisation** mais la **reconstruit** à la désérialisation (le paiement reçoit son contrat). `@JsonIgnore` exclut la propriété dans les deux sens : rien n'est ni écrit ni lu. Sur `Reservation.client`, une `@JsonBackReference` serait donc fausse : la propriété serait ignorée à la lecture et le POST créerait une réservation sans client.

## C. Postman et Swagger

| Outil | Ce qu'il apporte | Exemple concret de l'atelier |
|---|---|---|
| **Postman** | Tester : requêtes rejouables, assertions (`pm.test`) et scénarios, variables d'environnement (`{{baseUrl}}`, `{{contratId}}`). | GET `/api/contrats/9999` doit répondre **404** avec un corps `{ statut, message, horodatage }` ; POST `/api/paiements` doit répondre **405**. Le scénario « Créer un contrat avec ses paiements » puis « Lister les paiements » vérifie la cascade. |
| **Swagger UI** | Documenter et explorer : contrat OpenAPI généré depuis le code, bouton *Try it out*, schémas des entités. | `/api/swagger-ui.html` regroupe les endpoints par tag (Contrats, Paiements…) avec résumés et codes de réponse ; `/api/v3/api-docs` donne la description JSON. |

Ils sont complémentaires : Postman prouve que l'API **se comporte** comme prévu, Swagger montre **ce qu'elle promet** et reste synchronisé avec le code. **(à vérifier)** : compléter avec les statuts et durées réellement obtenus chez vous.

## D. Anomalies SonarQube for IDE

> L'analyse (clic droit sur `web.controller` > *Analyze with SonarQube for IDE*) est à lancer depuis IntelliJ ; les lignes ci-dessous sont à confirmer dans le panneau.

| Anomalie | Règle / explication | Correction |
|---|---|---|
| Entités JPA utilisées comme paramètre `@RequestBody` et comme type de retour (tous les contrôleurs) | S4684 : une entité persistante exposée par un endpoint couple l'API à la base (champs internes visibles, risque de *mass assignment*). **(à vérifier dans le panneau)** | Volontaire et provisoire (énoncé, section 1.3) : le Chapitre 10 introduit les DTO. |
| Injection par attribut (si un `@Autowired` sur champ est écrit à la place du constructeur) | S6813 : dépendance cachée, champ non `final`. | Les contrôleurs utilisent `@RequiredArgsConstructor` + champ `final` : non concernés. |
| *(à compléter avec le résultat réel de l'analyse : import inutile, etc.)* | | |

## E. Questions de compréhension

**1. Quel composant reçoit toutes les requêtes, et comment retrouve-t-il la méthode du contrôleur ?**
Le `DispatcherServlet` (point d'entrée unique). Il interroge le `HandlerMapping`, qui retrouve le contrôleur et la méthode à partir du verbe HTTP et de l'URL (`@RequestMapping` de classe + `@GetMapping`, `@PostMapping`… de méthode).

**2. Pourquoi `ContratController` dépend-il de `IContratService` et non de `ContratRepository` ni de `ContratServiceImpl` ?**
Un contrôleur mince ne contient ni logique métier ni accès aux données : il délègue au service (couches séparées). Dépendre de l'interface réduit le couplage : on peut changer l'implémentation ou injecter un faux service pour tester, sans modifier le contrôleur. Passer par le repository contournerait les règles de gestion du service (par exemple le refus d'un montant négatif).

**3. Pourquoi un POST renvoie-t-il 201 et un DELETE 204 ? Pourquoi une ressource absente donne-t-elle 404 et non 500 ?**
201 Created signifie « ressource créée » et le corps contient la ressource avec son identifiant ; 204 No Content signifie « suppression réussie, rien à renvoyer ». Une ressource absente n'est pas une panne du serveur mais une erreur du client (identifiant inexistant) : `ResourceNotFoundException` est traduite en 404 par le `GlobalExceptionHandler`. Un 500 est réservé aux erreurs inattendues du serveur.

**4. Pourquoi `PaiementController` n'expose-t-il que des GET ? Quelle réponse reçoit un POST `/api/paiements` ?**
Un paiement appartient à un contrat (composition, `cascade = ALL`) : il se crée, se modifie et se supprime via le contrat, pour garder le montant total cohérent. `IPaiementService` est en lecture seule et aucune méthode POST n'est mappée sur `/paiements` : Spring répond **405 Method Not Allowed**.

**5. Quels risques présente l'exposition directe des entités JPA ? Pourquoi `Contrat.paiements` utilise-t-il `@JsonManagedReference` alors que `Agence.vehicules` utilise `@JsonIgnore` ?**
Risques : couplage entre l'API et le schéma de la base, champs internes ou sensibles visibles, boucles de sérialisation sur les associations bidirectionnelles (`StackOverflowError`), proxies LAZY non sérialisables, et un client qui peut renseigner n'importe quel champ de l'entité. Les paiements sont une composition : ils sont créés dans le JSON de leur contrat, donc la liste doit être lue et écrite (`@JsonManagedReference` + `@JsonBackReference` côté `Paiement`). Un véhicule, lui, est créé seul et désigne son agence par identifiant (`{ "agence": { "idAgence": 1 } }`) : on ignore donc la liste inverse `Agence.vehicules` avec `@JsonIgnore`.
