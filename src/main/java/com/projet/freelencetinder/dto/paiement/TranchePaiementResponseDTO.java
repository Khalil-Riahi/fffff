/* ===== TranchePaiementResponseDTO.java ===== */
package com.projet.freelencetinder.dto.paiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.projet.freelencetinder.models.TranchePaiement.StatutTranche;

public class TranchePaiementResponseDTO {

    private Long id;
    private Integer ordre;
    private String titre;
    private BigDecimal montantBrut;
    private BigDecimal commissionPlateforme;
    private BigDecimal montantNetFreelance;
    private String devise;
    private StatutTranche statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDepot;
    private LocalDateTime dateValidation;
    private LocalDateTime dateVersement;
    private String paymeePaymentUrl;

    // Nouveaux indicateurs
    private boolean required;
    private boolean finale;
    private Long livrableAssocieId;

    /* ---------- getters / setters ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public BigDecimal getMontantBrut() { return montantBrut; }
    public void setMontantBrut(BigDecimal montantBrut) { this.montantBrut = montantBrut; }

    public BigDecimal getCommissionPlateforme() { return commissionPlateforme; }
    public void setCommissionPlateforme(BigDecimal commissionPlateforme) { this.commissionPlateforme = commissionPlateforme; }

    public BigDecimal getMontantNetFreelance() { return montantNetFreelance; }
    public void setMontantNetFreelance(BigDecimal montantNetFreelance) { this.montantNetFreelance = montantNetFreelance; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public StatutTranche getStatut() { return statut; }
    public void setStatut(StatutTranche statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public LocalDateTime getDateVersement() { return dateVersement; }
    public void setDateVersement(LocalDateTime dateVersement) { this.dateVersement = dateVersement; }

    public String getPaymeePaymentUrl() { return paymeePaymentUrl; }
    public void setPaymeePaymentUrl(String paymeePaymentUrl) { this.paymeePaymentUrl = paymeePaymentUrl; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public boolean isFinale() { return finale; }
    public void setFinale(boolean finale) { this.finale = finale; }
    public Long getLivrableAssocieId() { return livrableAssocieId; }
    public void setLivrableAssocieId(Long livrableAssocieId) { this.livrableAssocieId = livrableAssocieId; }
}
