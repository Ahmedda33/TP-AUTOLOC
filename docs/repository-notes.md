# Notes — Atelier 3 : couche Repository

## Choix d'interface

| Interface | Étend | Justification |
|---|---|---|
| IContratRepository | JpaRepository<Contrat, Long> | CRUD complet, `findAll` renvoie une `List`, `saveAndFlush` disponible. |
| IAgenceRepository | JpaRepository<Agence, Long> | Même modèle : CRUD + listes + tri/pagination. |
| IEmployeRepository | JpaRepository<Employe, Long> | Idem. |
| IVehiculeRepository | JpaRepository<Vehicule, Long> | Idem (entité centrale du modèle). |
| IEquipementRepository | JpaRepository<Equipement, Long> | Idem. |
| IClientRepository | JpaRepository<Client, Long> | Idem. |
| IReservationRepository | JpaRepository<Reservation, Long> | Idem. |
| IPaiementRepository | JpaRepository<Paiement, Long> | Lecture des paiements ; création/suppression via le `Contrat` (composition, `orphanRemoval`). |
| IMaintenanceRepository | JpaRepository<Maintenance, Long> | Idem. |

`JpaRepository` = `ListCrudRepository` + `ListPagingAndSortingRepository` + `QueryByExampleExecutor` + méthodes JPA
(`flush`, `saveAndFlush`, suppression en lot…). Attention : `deleteAllInBatch` contourne cascade et `orphanRemoval`.

## Table d'association Vehicule ↔ Equipement

Le `@ManyToMany` + `@JoinTable` a été remplacé par l'entité d'association `VehiculeEquipement`
(clé composite `VehiculeEquipementId` = `id_vehicule` + `id_equipement`, table `vehicule_equipement`).
`Vehicule` et `Equipement` ont chacun un `@OneToMany(mappedBy=…)` vers cette entité.

## Anomalies SonarQube for IDE (à compléter après analyse dans IntelliJ)

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| Interface `VehiculeRepository` mal nommée (convention `I…Repository`) | Convention de nommage AutoLoc | Renommée `IVehiculeRepository` |
| Enums dupliquées (`domain/CategorieVehicule`, `domain/StatutVehicule`) | Code mort / duplication | Fichiers inutilisés supprimés |
| `CommandLineRunner` réinsère 3 véhicules à chaque démarrage | Données dupliquées | Insertion seulement si la table est vide |
| imports `java.util.*` inutilisés ou trop larges dans les entités | Import inutilisé | Supprimés dans `Employe`, `Maintenance`, `Paiement`, `Reservation` ; remplacés par `java.util.List` ailleurs |

## Démonstration CRUD (Contrat ↔ Paiement)

Le `CommandLineRunner` `demoContrat` de `TpAutolocApplication` enchaîne, à chaque démarrage :
`save` (INSERT contrat + paiements), `findById`, `save` sur un contrat existant (SELECT puis UPDATE),
retrait d'un paiement de la liste (DELETE par `orphanRemoval`), puis `deleteById` (DELETE du paiement restant et du contrat).
Le résultat se lit dans les logs SQL ; rien ne reste en base après l'exécution.
