# Notes — Atelier 6 : Patrons de conception GoF et principes SOLID

> Dépôt : `tp-autoloc` — Documentation d'accompagnement de l'Atelier 6.

---

## 1. Partie 1 : Analyse de AutoLoc v0 (`AutoLocManager`)

### Question 1 : Combien de responsabilités possède la classe `AutoLocManager` ?
La classe `AutoLocManager` concentre au moins **5 responsabilités distinctes** :
1. **Calcul du prix de location** : application des barèmes et règles tarifaires selon la durée et le type de réservation.
2. **Gestion du contrat** : instanciation et logique de contrat.
3. **Persistance des données** : sauvegarde du contrat (`sauvegarderContrat()`) et enregistrement du paiement (`enregistrerPaiement()`).
4. **Notification par email** : envoi de l'email de confirmation au client (`envoyerEmail()`).
5. **Notification par SMS** : envoi de SMS (`envoyerSMS()`).

---

### Question 2 : Quels principes SOLID ne sont pas respectés ?

1. **SRP (Single Responsibility Principle - Responsabilité unique)** :
   - *Règle* : Une classe ne doit avoir qu'une seule raison de changer.
   - *Violation* : `AutoLocManager` change si la formule de tarification change, si la base de données change, ou si le format/canal d'envoi d'email ou SMS change.

2. **OCP (Open / Closed Principle - Ouvert / Fermé)** :
   - *Règle* : Les entités logicielles doivent être ouvertes à l'extension mais fermées à la modification.
   - *Violation* : Pour chaque nouveau type de tarif ou nouveau canal de notification, il faut modifier le code existant avec de nouveaux `if / else if`.

3. **DIP (Dependency Inversion Principle - Inversion des dépendances)** :
   - *Règle* : Les modules de haut niveau ne doivent pas dépendre des modules de bas niveau ; les deux doivent dépendre d'abstractions.
   - *Violation* : `AutoLocManager` dépend directement d'implémentations concrètes (appels directs dans la méthode sans passer par des interfaces injectées).

---

### Question 3 : Que se passe-t-il si AutoLoc ajoute un nouveau type de réservation (ex: "Pleinesaison") ?

- **Impact** : Il faut rouvrir la classe `AutoLocManager`, ajouter une nouvelle clause `else if (typeReservation.equals("Pleinesaison"))`, et modifier la méthode `louerVehicule`.
- **Risques** : Risque d'introduire une régression dans les calculs existants, nécessité de re-tester l'ensemble des cas, duplication potentielle.
- **Principe violé** : Violation directe du principe **OCP (Open/Closed Principle)**.

---

## 2. Refactoring appliqué avec les Patrons GoF

### A. Patron Strategy (Tarification)
- **Interface** : `ITarificationStrategy` (`calculerTarif(Vehicule, Reservation)`, `getCode()`)
- **Implémentations concrètes** :
  - `TarifStandardStrategy` : calcul au tarif journalier de base.
  - `TarifWeekendStrategy` : remise week-end (10 %).
  - `TarifLongueDureeStrategy` : remise séjour ≥ 7 jours (20 %).
- **Bénéfice** : L'ajout d'une nouvelle stratégie (ex: `TarifPleineSaisonStrategy`) se fait simplement en créant une nouvelle classe `@Component` implémentant `ITarificationStrategy`, sans toucher au code existant (**respect de l'OCP**).

---

### B. Patron Factory Method (Sélection et Création)
- **`TarificationStrategyFactory`** :
  - Injecte automatiquement toutes les implémentations Spring via `List<ITarificationStrategy>`.
  - Résout dynamiquement la stratégie adaptée selon la durée et les dates de réservation (`resoudre(Reservation)`).
- **`NotificationFactory`** :
  - Centralise l'accès au bon canal d'observation (`EMAIL`, `SMS`) sans exposer les classes concrètes au code appelant.

---

### C. Patron Observer (Cycle de vie des Réservations)
- **Interface** : `IReservationObserver` (`onReservationConfirmee`, `onReservationAnnulee`)
- **Sujet observable** : `ReservationSubject`
  - Maintient la liste des observateurs injectés par Spring.
  - Déclenche `notifierConfirmation()` et `notifierAnnulation()`.
- **Observateurs concrets** :
  - `EmailNotificationObserver` : journalise la confirmation ou l'annulation par email.
  - `SmsNotificationObserver` : journalise la notification SMS.
- **Intégration métier** :
  - `ReservationServiceImpl.create()` déclenche automatiquement `reservationSubject.notifierConfirmation(saved)`.
  - `ReservationServiceImpl.annulerReservation(id)` passe le statut à `ANNULEE` et déclenche `reservationSubject.notifierAnnulation(updated)`.

---

## 3. Synthèse des classes et packages ajoutés

```
tn.esprit.tpautoloc
├── AutoLocManager.java                                # Version v0 d'analyse (Partie 1)
└── service/
    ├── strategy/
    │   ├── ITarificationStrategy.java                 # Interface Strategy
    │   ├── TarifStandardStrategy.java                 # Stratégie Standard
    │   ├── TarifWeekendStrategy.java                  # Stratégie Week-end (-10%)
    │   └── TarifLongueDureeStrategy.java              # Stratégie Longue durée (-20%)
    ├── factory/
    │   ├── TarificationStrategyFactory.java           # Factory de stratégies
    │   └── NotificationFactory.java                   # Factory d'observateurs
    └── observer/
        ├── IReservationObserver.java                  # Interface Observer
        ├── ReservationSubject.java                    # Sujet observable
        ├── EmailNotificationObserver.java             # Observateur Email
        └── SmsNotificationObserver.java               # Observateur SMS
```
