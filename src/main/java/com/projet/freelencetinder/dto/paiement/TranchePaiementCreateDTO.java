/* ===== TranchePaiementCreateDTO.java ===== */
package com.projet.freelencetinder.dto.paiement;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

/**
 * Représente les données nécessaires à la création d'une tranche de paiement.
 * Le champ « montantBrut » reprend le même nom que dans l'entité afin de
 * limiter les risques de confusion lors du mapping JSON <-> DTO <-> Entity.
 */
public class TranchePaiementCreateDTO {

    @NotNull @Min(1)
    private Integer ordre;

    @NotBlank @Size(max = 160)
    private String titre;

    /** Montant total facturé au client avant commissions. */
    @NotNull @DecimalMin("0.0")
    @com.fasterxml.jackson.annotation.JsonAlias("montant")
    private BigDecimal montantBrut;

    private String devise = "TND";

    @NotNull
    private Long missionId;

    /* ---------- getters / setters ---------- */
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public BigDecimal getMontantBrut() { return montantBrut; }
    public void setMontantBrut(BigDecimal montantBrut) { this.montantBrut = montantBrut; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
}
