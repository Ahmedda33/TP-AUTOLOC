# Notes — Atelier 4 : IoC, injection de dépendances et couche Service

## A. Modes d'injection comparés (étape 2)

| Critère | Constructeur (`@RequiredArgsConstructor`, champ `final`) | Attribut (`@Autowired` sur le champ) |
|---|---|---|
| Champ `final` possible ? | Oui | Non : le champ doit être modifiable pour que Spring l'affecte après la construction |
| Dépendance visible dans la signature de la classe ? | Oui : elle apparaît dans le constructeur | Non : elle est cachée dans le corps de la classe |
| Classe utilisable avec `new` hors conteneur sans dépendance `null` ? | Oui : `new ContratServiceImpl(repo)` oblige à fournir le repository (aussi pratique pour un test unitaire avec un faux repository) | Non : `new ContratServiceImpl()` donne un objet dont `contratRepository` vaut `null` (`NullPointerException` à la première utilisation) |
| Signalé par SonarQube for IDE ? | Non | Oui : règle S6813 (« Remove this field injection and use constructor injection instead »), à vérifier dans le panneau |

Conclusion : on garde l'injection par constructeur (convention AutoLoc). Le service ne peut jamais exister dans un état incomplet.

## B. Services

| Service | Dépendances injectées | Mode | Justification |
|---|---|---|---|
| AgenceServiceImpl | IAgenceRepository | Constructeur | Une seule dépendance obligatoire, typée par interface, champ `final` ; service sans état. |
| EmployeServiceImpl | IEmployeRepository | Constructeur | Idem. |
| VehiculeServiceImpl | IVehiculeRepository | Constructeur | Idem. |
| EquipementServiceImpl | IEquipementRepository | Constructeur | Idem. |
| ClientServiceImpl | IClientRepository | Constructeur | Idem. |
| ReservationServiceImpl | IReservationRepository | Constructeur | Idem. Aucune dépendance vers un autre service : pas de cycle possible. |
| ContratServiceImpl | IContratRepository | Constructeur | Idem. Les paiements passent par le contrat (cascade), pas par un autre service. |
| PaiementServiceImpl | IPaiementRepository | Constructeur | Idem. Lecture seule (`findById`, `findAll`). |
| MaintenanceServiceImpl | IMaintenanceRepository | Constructeur | Idem. |

Règles de gestion implémentées (selon les attributs réels du modèle) :

| Service | Règle |
|---|---|
| Agence | `nom` obligatoire (création et modification). |
| Employe | `nom` et `role` obligatoires. |
| Vehicule | `tarifJournalier` négatif refusé. |
| Equipement | `libelle` obligatoire. Pas d'attribut « quantité » dans le modèle : seule la règle du libellé est appliquée. |
| Client | `nom` et `email` obligatoires. |
| Reservation | `dateFin` antérieure à `dateDebut` refusée. |
| Contrat | `montantTotal` négatif refusé ; `deleteById` supprime aussi les paiements (cascade `ALL`). |
| Paiement | Lecture seule. |
| Maintenance | `dateFin` antérieure à `dateDebut` refusée. Pas d'attribut « coût » dans le modèle : seule la règle des dates est appliquée. |

Pour tous : `create` refuse un id non `null` ; `findById` et `deleteById` lèvent `ResourceNotFoundException` si l'id n'existe pas ; `update` charge l'entité existante, recopie les attributs simples (ni l'identifiant, ni les associations, ni les collections), puis appelle `save`. `count()` et `existsById(Long id)` délèguent au repository : ils servent aux tests de la Partie 2, qui passent uniquement par la couche Service.

## C. Messages d'erreur du conteneur (étape 3)

**Message 1 — `ContratController` ne trouve pas de bean `IContratService`**
- Exception : `NoSuchBeanDefinitionException`, enveloppée dans une `UnsatisfiedDependencyException` (« APPLICATION FAILED TO START »).
- Bean concerné : `ContratController`, paramètre 0 du constructeur, de type `IContratService`.
- Causes probables : (1) `@Service` oubliée sur `ContratServiceImpl` : la classe n'est alors pas détectée par le scan ; (2) `ContratServiceImpl` placée en dehors du package racine `tn.esprit.tpautoloc` (le scan ne couvre que ce package et ses sous-packages).
- Correction : ajouter `@Service` sur `ContratServiceImpl` ; la déplacer dans un sous-package de `tn.esprit.tpautoloc`.

**Message 2 — deux beans pour un même type (`emailNotificateur` et `smsNotificateur`)**
- Exception : `NoUniqueBeanDefinitionException` (« required a single bean, but 2 were found »).
- Cause : `NotificationController` demande un seul bean du type, et deux implémentations sont disponibles.
- Correction 1 : `@Primary` sur la **classe** `EmailNotificateur` (candidat par défaut).
- Correction 2 : `@Qualifier("emailNotificateur")` sur le **paramètre du constructeur** de `NotificationController` (choix explicite à l'endroit de l'injection ; il l'emporte sur `@Primary`).

**Message 3 — cycle `clientServiceImpl` ⇄ `reservationServiceImpl`**
- Exception : `BeanCurrentlyInCreationException` (dépendance circulaire).
- Pourquoi l'injection par constructeur rend ce cycle impossible à contourner : pour construire `clientServiceImpl`, Spring doit déjà avoir `reservationServiceImpl`, qui lui-même a besoin de `clientServiceImpl`. Avec des champs `final` affectés dans le constructeur, aucun des deux ne peut être créé en premier. (L'injection par attribut ou par setter masquerait le problème en injectant un bean pas encore complet, ce qui est justement ce qu'on veut éviter.)
- Piste de conception : extraire la responsabilité commune dans un troisième composant dont les deux services dépendent (par exemple un service ou une classe dédiée à la logique partagée), ou faire dépendre l'un des deux services directement du repository de l'autre au lieu du service.

## D. Anomalies SonarQube for IDE (étape 4)

> Les règles ci-dessous sont celles annoncées par l'énoncé ; leur affichage dans le panneau SonarQube for IDE n'a pas été vérifié depuis IntelliJ.

| Anomalie | Règle / explication | Correction apportée |
|---|---|---|
| Injection par attribut (`@Autowired` sur le champ de `ContratServiceImpl`, étape 2) | S6813 : dépendance cachée, champ non `final`, `null` hors conteneur | Retour à l'injection par constructeur (`@RequiredArgsConstructor` + champ `final`). |
| `ResourceNotFoundException` remplacée par `RuntimeException` dans `findById` (expérience de l'étape 4) | S112 : lever une exception générique ; l'appelant ne peut pas distinguer « absent » d'une autre erreur | Code restauré : exception métier `ResourceNotFoundException`. |
| *(à compléter après l'analyse des packages `service` et `exception` dans IntelliJ)* | | |

## E. Questions de compréhension

**1. Où est le `new` de `ContratServiceImpl` et qui l'exécute ?**
Il n'y en a pas dans notre code. Au démarrage, `SpringApplication.run(...)` crée l'`ApplicationContext` ; le scan détecte `@Service`, puis le conteneur appelle lui-même le constructeur (par réflexion) en lui passant le proxy `IContratRepository` généré par Spring Data.

**2. Pourquoi un contrôleur dépendra-t-il de `IContratService` et non de `ContratServiceImpl` ?**
Pour réduire le couplage : le contrôleur ne connaît que le contrat (l'interface). On peut changer d'implémentation sans toucher au contrôleur, et le tester avec un faux service. C'est aussi ce qui permet à Spring d'envelopper le service dans un proxy (transactions, par exemple).

**3. Pourquoi un service singleton doit-il rester sans état ? Donner un exemple de bug.**
Il y a une seule instance, partagée par toutes les requêtes et tous les threads. Un attribut modifiable est donc partagé entre utilisateurs. Exemple : un champ `private Contrat contratCourant;` renseigné dans `findById` puis lu dans `update` ; deux requêtes simultanées s'écrasent mutuellement et l'utilisateur A modifie le contrat de l'utilisateur B.

**4. Pourquoi `update` charge-t-il l'entité existante avant d'appeler `save` ?**
Passer directement l'objet reçu à `save` (id renseigné) déclenche un `merge` qui écrase **toutes** les colonnes : les champs non envoyés deviennent `null`, et une collection absente (`paiements`) pourrait être vidée, surtout si la relation est en `orphanRemoval`. En chargeant l'existant, on vérifie aussi qu'il existe (`ResourceNotFoundException`) et on ne modifie que les attributs voulus, sans toucher à l'identifiant ni aux collections.

**5. Quelle différence entre `@Component` et `@Bean` ? Entre `@Primary` et `@Qualifier` ? Pourquoi `IPaiementService` n'expose-t-il ni création, ni modification, ni suppression ?**
- `@Component` se place sur une classe qu'on écrit, détectée par le scan. `@Bean` se place sur une méthode d'une classe `@Configuration` : déclaration explicite, utile pour une classe qu'on ne peut pas annoter (bibliothèque, JDK) ou une construction particulière.
- `@Primary` (sur la classe du bean) désigne le candidat par défaut quand plusieurs beans ont le même type. `@Qualifier("nom")` (au point d'injection) choisit un bean précis par son nom et l'emporte sur `@Primary`.
- Un `Paiement` appartient à un `Contrat` (composition : cascade depuis le contrat). Créer, modifier ou supprimer un paiement directement contournerait le contrat et pourrait rendre le montant total incohérent ; ces opérations passent donc par le `Contrat`, et `IPaiementService` reste en lecture seule.
