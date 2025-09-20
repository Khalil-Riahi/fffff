package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.projet.freelencetinder.models.TranchePaiement;

/**
 * DTO minimal pour les tranches de paiement dans la vue détail mission.
 * Contient les informations essentielles avec masquage des montants si nécessaire.
 */
public class TrancheMiniDTO {

    private Long id;
    private Integer ordre;
    private String titre;
    private TranchePaiement.StatutTranche statut;
    private BigDecimal montantBrut; // null si masqué
    private BigDecimal montantNetFreelance; // null si masqué
    private String devise;
    private boolean required;
    private boolean finale;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDepot;
    private LocalDateTime dateValidation;
    private LocalDateTime dateVersement;
    private String paymentUrl; // null si masqué
    private Long livrableId; // null si pas de livrable associé
    private String livrableTitre;
    private TranchePaiement.StatutTranche livrableStatus; // statut du livrable associé
    private LocalDateTime livrableDateEnvoi;
    private boolean canOpenPaymentLink; // false pour les freelances (read-only)
    private boolean paid; // true si statut = VERSEE_FREELANCE
    private boolean deliveryAccepted; // true si livrable associé et validé

    /* ===================== Getters & Setters ===================== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public TranchePaiement.StatutTranche getStatut() { return statut; }
    public void setStatut(TranchePaiement.StatutTranche statut) { this.statut = statut; }

    public BigDecimal getMontantBrut() { return montantBrut; }
    public void setMontantBrut(BigDecimal montantBrut) { this.montantBrut = montantBrut; }

    public BigDecimal getMontantNetFreelance() { return montantNetFreelance; }
    public void setMontantNetFreelance(BigDecimal montantNetFreelance) { this.montantNetFreelance = montantNetFreelance; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public boolean isFinale() { return finale; }
    public void setFinale(boolean finale) { this.finale = finale; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public LocalDateTime getDateVersement() { return dateVersement; }
    public void setDateVersement(LocalDateTime dateVersement) { this.dateVersement = dateVersement; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public Long getLivrableId() { return livrableId; }
    public void setLivrableId(Long livrableId) { this.livrableId = livrableId; }

    public String getLivrableTitre() { return livrableTitre; }
    public void setLivrableTitre(String livrableTitre) { this.livrableTitre = livrableTitre; }

    public TranchePaiement.StatutTranche getLivrableStatus() { return livrableStatus; }
    public void setLivrableStatus(TranchePaiement.StatutTranche livrableStatus) { this.livrableStatus = livrableStatus; }

    public LocalDateTime getLivrableDateEnvoi() { return livrableDateEnvoi; }
    public void setLivrableDateEnvoi(LocalDateTime livrableDateEnvoi) { this.livrableDateEnvoi = livrableDateEnvoi; }

    public boolean isCanOpenPaymentLink() { return canOpenPaymentLink; }
    public void setCanOpenPaymentLink(boolean canOpenPaymentLink) { this.canOpenPaymentLink = canOpenPaymentLink; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public boolean isDeliveryAccepted() { return deliveryAccepted; }
    public void setDeliveryAccepted(boolean deliveryAccepted) { this.deliveryAccepted = deliveryAccepted; }
}