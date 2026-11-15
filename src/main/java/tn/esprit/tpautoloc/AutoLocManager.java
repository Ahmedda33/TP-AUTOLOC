package tn.esprit.tpautoloc;

/**
 * AutoLoc v0 — Classe problématique (Atelier 6, Partie 1).
 * Concentre trop de responsabilités : calcul du prix, notification, sauvegarde.
 * Violations SOLID : SRP (plusieurs responsabilités), OCP (ajout d'un type = modification du code).
 */
public class AutoLocManager {
    public void louerVehicule(String typeReservation, int nombreJours, String clientEmail) {
        double prix = 0;
        if (typeReservation.equals("LongDuree")) {
            prix = nombreJours * 120.0;
        } else if (typeReservation.equals("Weekend")) {
            prix = nombreJours * 180.0;
        } else if (typeReservation.equals("Standard")) {
            prix = nombreJours * 150.0;
        }
        System.out.println("Contrat créé");
        System.out.println("Prix total : " + prix);
        envoyerEmail(clientEmail);
        sauvegarderContrat();
    }

    private void envoyerEmail(String email) {
        System.out.println("Email envoyé à " + email);
    }

    private void envoyerSMS(String sms) {
        System.out.println("SMS envoyé à " + sms);
    }

    private void sauvegarderContrat() {
        System.out.println("Contrat sauvegardé");
    }
}
