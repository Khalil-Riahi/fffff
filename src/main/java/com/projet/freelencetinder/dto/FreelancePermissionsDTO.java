package com.projet.freelencetinder.dto;

/**
 * DTO pour les permissions du freelance dans la vue détail mission.
 * Définit les actions autorisées selon le statut d'assignation et de la mission.
 */
public class FreelancePermissionsDTO {

    private boolean canApply; // true si mission EN_ATTENTE, non expirée, et freelance non assigné
    private boolean canWithdraw; // true si candidature envoyée mais non sélectionné
    private boolean canDeliver; // true si freelance assigné et mission EN_COURS
    private boolean canSeePayments; // true si freelance assigné
    private boolean canMessage; // true si freelance assigné
    private boolean canSeeClientFull; // true si freelance assigné

    /* ===================== Getters & Setters ===================== */
    public boolean isCanApply() { return canApply; }
    public void setCanApply(boolean canApply) { this.canApply = canApply; }

    public boolean isCanWithdraw() { return canWithdraw; }
    public void setCanWithdraw(boolean canWithdraw) { this.canWithdraw = canWithdraw; }

    public boolean isCanDeliver() { return canDeliver; }
    public void setCanDeliver(boolean canDeliver) { this.canDeliver = canDeliver; }

    public boolean isCanSeePayments() { return canSeePayments; }
    public void setCanSeePayments(boolean canSeePayments) { this.canSeePayments = canSeePayments; }

    public boolean isCanMessage() { return canMessage; }
    public void setCanMessage(boolean canMessage) { this.canMessage = canMessage; }

    public boolean isCanSeeClientFull() { return canSeeClientFull; }
    public void setCanSeeClientFull(boolean canSeeClientFull) { this.canSeeClientFull = canSeeClientFull; }
}
