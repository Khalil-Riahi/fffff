package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Entity
@Table(
    name = "feedback",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_feedback_unique", columnNames = {"mission_id", "issuer_id", "target_id", "role"})
    },
    indexes = {
        @Index(name = "idx_feedback_published_target", columnList = "published_at, target_id"),
        @Index(name = "idx_feedback_mission", columnList = "mission_id"),
        @Index(name = "idx_feedback_idem", columnList = "idempotency_key")
    }
)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Feedback {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotNull
    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @NotNull
    @Column(name = "issuer_id", nullable = false)
    private Long issuerId;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedbackRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FeedbackStatus status = FeedbackStatus.DRAFT;

    @DecimalMin("1.0")
    @DecimalMax("5.0")
    @Digits(integer = 1, fraction = 1)
    @Column(name = "overall_rating", precision = 2, scale = 1)
    private BigDecimal overallRating;

    @Size(min = 30, max = 800)
    @Column(length = 800)
    private String comment;

    @Size(max = 8)
    private String language;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Size(max = 128)
    @Column(name = "ip_hash", length = 128)
    private String ipHash;

    @Size(max = 128)
    @Column(name = "ua_hash", length = 128)
    private String userAgentHash;

    @Size(max = 160)
    @Column(name = "visibility_reason", length = 160)
    private String visibilityReason;

    @Size(max = 64)
    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private List<FeedbackCriterionScore> scores = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.lastUpdatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedAt = Instant.now();
    }

    public void addScore(FeedbackCriterionScore s) {
        if (s != null) {
            s.setFeedback(this);
            this.scores.add(s);
        }
    }

    public void clearScores() {
        this.scores.forEach(sc -> sc.setFeedback(null));
        this.scores.clear();
    }

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }
    public Long getIssuerId() { return issuerId; }
    public void setIssuerId(Long issuerId) { this.issuerId = issuerId; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public FeedbackRole getRole() { return role; }
    public void setRole(FeedbackRole role) { this.role = role; }
    public FeedbackStatus getStatus() { return status; }
    public void setStatus(FeedbackStatus status) { this.status = status; }
    public BigDecimal getOverallRating() { return overallRating; }
    public void setOverallRating(BigDecimal overallRating) { this.overallRating = overallRating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    public String getIpHash() { return ipHash; }
    public void setIpHash(String ipHash) { this.ipHash = ipHash; }
    public String getUserAgentHash() { return userAgentHash; }
    public void setUserAgentHash(String userAgentHash) { this.userAgentHash = userAgentHash; }
    public String getVisibilityReason() { return visibilityReason; }
    public void setVisibilityReason(String visibilityReason) { this.visibilityReason = visibilityReason; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public List<FeedbackCriterionScore> getScores() { return scores; }
    public void setScores(List<FeedbackCriterionScore> scores) { this.scores = scores; }
}


