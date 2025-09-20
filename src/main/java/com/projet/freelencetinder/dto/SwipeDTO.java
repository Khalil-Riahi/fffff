package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;

import com.projet.freelencetinder.models.Mission.Gouvernorat;
import com.projet.freelencetinder.models.Swipe.Decision;
import com.projet.freelencetinder.models.Swipe.RaisonRejet;
import com.projet.freelencetinder.models.Swipe.Source;
import com.projet.freelencetinder.models.Utilisateur.Langue;

/**
 * Représentation d’un swipe côté freelance.
 */
public class SwipeDTO {

    private Long id;
    private Long missionId;
    private Long freelanceId;
    private Decision decision;
    private LocalDateTime dateSwipe;

    /* Analytics optionnelles */
    private boolean generatedMatch;
    private Long dwellTimeMs;

    /* ======== AJOUTS ======== */
    private LocalDateTime dateAffichage;
    private Long latenceAffichageMs;
    private Integer positionDansPile;

    private String sessionId;
    private Source source;                   // WEB/ANDROID/IOS
    private Langue langueUi;                 // FR/AR/EN
    private Gouvernorat gouvernoratFreelance;

    private Boolean superLike;               // true si super-like
    private RaisonRejet raisonRejet;
    private String raisonRejetDetails;

    private String messageIntroAuto;
    private String algoVersion;

    private Boolean estUndo;                 // undo du swipe
    private Long annuleSwipeId;

    private String ipHash;
    private String campagneAb;

    public SwipeDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public Long getFreelanceId() { return freelanceId; }
    public void setFreelanceId(Long freelanceId) { this.freelanceId = freelanceId; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public LocalDateTime getDateSwipe() { return dateSwipe; }
    public void setDateSwipe(LocalDateTime dateSwipe) { this.dateSwipe = dateSwipe; }

    public boolean isGeneratedMatch() { return generatedMatch; }
    public void setGeneratedMatch(boolean generatedMatch) { this.generatedMatch = generatedMatch; }

    public Long getDwellTimeMs() { return dwellTimeMs; }
    public void setDwellTimeMs(Long dwellTimeMs) { this.dwellTimeMs = dwellTimeMs; }

    public LocalDateTime getDateAffichage() { return dateAffichage; }
    public void setDateAffichage(LocalDateTime dateAffichage) { this.dateAffichage = dateAffichage; }

    public Long getLatenceAffichageMs() { return latenceAffichageMs; }
    public void setLatenceAffichageMs(Long latenceAffichageMs) { this.latenceAffichageMs = latenceAffichageMs; }

    public Integer getPositionDansPile() { return positionDansPile; }
    public void setPositionDansPile(Integer positionDansPile) { this.positionDansPile = positionDansPile; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }

    public Langue getLangueUi() { return langueUi; }
    public void setLangueUi(Langue langueUi) { this.langueUi = langueUi; }

    public Gouvernorat getGouvernoratFreelance() { return gouvernoratFreelance; }
    public void setGouvernoratFreelance(Gouvernorat gouvernoratFreelance) { this.gouvernoratFreelance = gouvernoratFreelance; }

    public Boolean getSuperLike() { return superLike; }
    public void setSuperLike(Boolean superLike) { this.superLike = superLike; }

    public RaisonRejet getRaisonRejet() { return raisonRejet; }
    public void setRaisonRejet(RaisonRejet raisonRejet) { this.raisonRejet = raisonRejet; }

    public String getRaisonRejetDetails() { return raisonRejetDetails; }
    public void setRaisonRejetDetails(String raisonRejetDetails) { this.raisonRejetDetails = raisonRejetDetails; }

    public String getMessageIntroAuto() { return messageIntroAuto; }
    public void setMessageIntroAuto(String messageIntroAuto) { this.messageIntroAuto = messageIntroAuto; }

    public String getAlgoVersion() { return algoVersion; }
    public void setAlgoVersion(String algoVersion) { this.algoVersion = algoVersion; }

    public Boolean getEstUndo() { return estUndo; }
    public void setEstUndo(Boolean estUndo) { this.estUndo = estUndo; }

    public Long getAnnuleSwipeId() { return annuleSwipeId; }
    public void setAnnuleSwipeId(Long annuleSwipeId) { this.annuleSwipeId = annuleSwipeId; }

    public String getIpHash() { return ipHash; }
    public void setIpHash(String ipHash) { this.ipHash = ipHash; }

    public String getCampagneAb() { return campagneAb; }
    public void setCampagneAb(String campagneAb) { this.campagneAb = campagneAb; }
}
