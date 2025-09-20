package com.projet.freelencetinder.models;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "feedback_window", uniqueConstraints = {
    @UniqueConstraint(name = "uk_feedback_window_mission", columnNames = {"mission_id"})
}, indexes = {
    @Index(name = "idx_feedback_window_expires", columnList = "expires_at")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FeedbackWindow {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @NotNull
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @NotNull
    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "opened_at", nullable = false, updatable = false)
    private Instant openedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "client_submitted", nullable = false)
    private boolean clientSubmitted = false;

    @Column(name = "freelancer_submitted", nullable = false)
    private boolean freelancerSubmitted = false;

    @Column(name = "auto_published_at")
    private Instant autoPublishedAt;

    @PrePersist
    protected void onCreate() {
        if (openedAt == null) openedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }
    public Instant getOpenedAt() { return openedAt; }
    public void setOpenedAt(Instant openedAt) { this.openedAt = openedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isClientSubmitted() { return clientSubmitted; }
    public void setClientSubmitted(boolean clientSubmitted) { this.clientSubmitted = clientSubmitted; }
    public boolean isFreelancerSubmitted() { return freelancerSubmitted; }
    public void setFreelancerSubmitted(boolean freelancerSubmitted) { this.freelancerSubmitted = freelancerSubmitted; }
    public Instant getAutoPublishedAt() { return autoPublishedAt; }
    public void setAutoPublishedAt(Instant autoPublishedAt) { this.autoPublishedAt = autoPublishedAt; }
}


