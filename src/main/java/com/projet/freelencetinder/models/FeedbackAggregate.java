package com.projet.freelencetinder.models;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "feedback_aggregate", indexes = {
    @Index(name = "idx_feedback_agg_target_window", columnList = "target_id, window_days")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FeedbackAggregate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Audience audience; // FREELANCE ou CLIENT

    @Column(name = "window_days", nullable = false)
    private int windowDays; // 90, 180

    @Digits(integer = 1, fraction = 1)
    @Column(name = "avg_rating", precision = 2, scale = 1)
    private BigDecimal avgRating;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount;

    @Digits(integer = 1, fraction = 1)
    @Column(name = "decayed_avg", precision = 2, scale = 1)
    private BigDecimal decayedAvgRating;

    @Digits(integer = 1, fraction = 2)
    @Column(name = "dispute_rate", precision = 3, scale = 2)
    private BigDecimal disputeRate;

    @Column(name = "last_computed_at")
    private Instant lastComputedAt;

    public Long getId() { return id; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Audience getAudience() { return audience; }
    public void setAudience(Audience audience) { this.audience = audience; }
    public int getWindowDays() { return windowDays; }
    public void setWindowDays(int windowDays) { this.windowDays = windowDays; }
    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public BigDecimal getDecayedAvgRating() { return decayedAvgRating; }
    public void setDecayedAvgRating(BigDecimal decayedAvgRating) { this.decayedAvgRating = decayedAvgRating; }
    public BigDecimal getDisputeRate() { return disputeRate; }
    public void setDisputeRate(BigDecimal disputeRate) { this.disputeRate = disputeRate; }
    public Instant getLastComputedAt() { return lastComputedAt; }
    public void setLastComputedAt(Instant lastComputedAt) { this.lastComputedAt = lastComputedAt; }
}


