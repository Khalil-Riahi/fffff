package com.projet.freelencetinder.dto;

public class PaiementRequestDTO {

    private Long clientId;
    private Long missionId;
    private Long freelanceId;
    private Double montant;
    private String devise = "TND";
    private String description;

    /* ---------- Constructeurs ---------- */
    public PaiementRequestDTO() {}

    public PaiementRequestDTO(Long clientId,
                              Long missionId,
                              Long freelanceId,
                              Double montant,
                              String devise,
                              String description) {
        this.clientId     = clientId;
        this.missionId    = missionId;
        this.freelanceId  = freelanceId;
        this.montant      = montant;
        this.devise       = devise;
        this.description  = description;
    }

    /* ---------- Getters / Setters ---------- */
    public Long getClientId()     { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getMissionId()    { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public Long getFreelanceId()  { return freelanceId; }
    public void setFreelanceId(Long freelanceId) { this.freelanceId = freelanceId; }

    public Double getMontant()    { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public String getDevise()     { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public String getDescription(){ return description; }
    public void setDescription(String description) { this.description = description; }
}
