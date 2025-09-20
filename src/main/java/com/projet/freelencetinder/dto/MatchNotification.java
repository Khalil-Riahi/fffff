package com.projet.freelencetinder.dto;

import java.time.Instant;

import com.projet.freelencetinder.models.Mission.Categorie;
import com.projet.freelencetinder.models.Mission.Gouvernorat;

/** Payload envoyé sur /user/{id}/queue/matches */
public class MatchNotification {

    private Long   conversationId;
    private Long   missionId;
    private Long   clientId;
    private Long   freelanceId;
    private String missionTitre;
    private String clientNom;
    private String freelanceNom;
    private String clientPhotoUrl;    // AJOUT antérieur
    private String freelancePhotoUrl; // AJOUT antérieur
    private Instant sentAt = Instant.now();

    /* ======== AJOUTS ======== */
    private Boolean superLike;        // le match a été déclenché par un super-like côté freelance ?
    private Boolean superInvite;      // … ou par un super-invite côté client ?
    private Gouvernorat missionGouvernorat; // Tunisie
    private Categorie   missionCategorie;

    public MatchNotification() { }

    public MatchNotification(Long conversationId,
                             Long missionId,
                             Long clientId,
                             Long freelanceId,
                             String missionTitre,
                             String clientNom,
                             String freelanceNom,
                             String clientPhotoUrl,
                             String freelancePhotoUrl) {
        this.conversationId = conversationId;
        this.missionId      = missionId;
        this.clientId       = clientId;
        this.freelanceId    = freelanceId;
        this.missionTitre   = missionTitre;
        this.clientNom      = clientNom;
        this.freelanceNom   = freelanceNom;
        this.clientPhotoUrl = clientPhotoUrl;
        this.freelancePhotoUrl = freelancePhotoUrl;
    }

    /* ---------- getters & setters ---------- */
    public Long    getConversationId() { return conversationId; }
    public void    setConversationId(Long conversationId){ this.conversationId = conversationId; }

    public Long    getMissionId()      { return missionId; }
    public void    setMissionId(Long missionId)           { this.missionId = missionId; }

    public Long    getClientId()       { return clientId; }
    public void    setClientId(Long clientId)             { this.clientId = clientId; }

    public Long    getFreelanceId()    { return freelanceId; }
    public void    setFreelanceId(Long freelanceId)       { this.freelanceId = freelanceId; }

    public String  getMissionTitre()   { return missionTitre; }
    public void    setMissionTitre(String missionTitre)   { this.missionTitre = missionTitre; }

    public String  getClientNom()      { return clientNom; }
    public void    setClientNom(String clientNom)         { this.clientNom = clientNom; }

    public String  getFreelanceNom()   { return freelanceNom; }
    public void    setFreelanceNom(String freelanceNom)   { this.freelanceNom = freelanceNom; }

    public Instant getSentAt()         { return sentAt; }
    public void    setSentAt(Instant sentAt)              { this.sentAt = sentAt; }

    public String getClientPhotoUrl() { return clientPhotoUrl; }
    public void setClientPhotoUrl(String clientPhotoUrl) { this.clientPhotoUrl = clientPhotoUrl; }

    public String getFreelancePhotoUrl() { return freelancePhotoUrl; }
    public void setFreelancePhotoUrl(String freelancePhotoUrl) { this.freelancePhotoUrl = freelancePhotoUrl; }

    // AJOUTS
    public Boolean getSuperLike() { return superLike; }
    public void setSuperLike(Boolean superLike) { this.superLike = superLike; }

    public Boolean getSuperInvite() { return superInvite; }
    public void setSuperInvite(Boolean superInvite) { this.superInvite = superInvite; }

    public Gouvernorat getMissionGouvernorat() { return missionGouvernorat; }
    public void setMissionGouvernorat(Gouvernorat missionGouvernorat) { this.missionGouvernorat = missionGouvernorat; }

    public Categorie getMissionCategorie() { return missionCategorie; }
    public void setMissionCategorie(Categorie missionCategorie) { this.missionCategorie = missionCategorie; }
}
