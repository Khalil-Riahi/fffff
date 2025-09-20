package com.projet.freelencetinder.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "feedback_criterion_score", indexes = {
    @Index(name = "idx_feedback_score_feedback", columnList = "feedback_id")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FeedbackCriterionScore {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CriterionType criterion;

    @Column(nullable = false)
    @NotNull
    @Min(1) @Max(5)
    private Integer score;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Digits(integer = 1, fraction = 2)
    @Column(precision = 3, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private WeightSource weightSource = WeightSource.STATIC;

    public Long getId() { return id; }
    public Feedback getFeedback() { return feedback; }
    public void setFeedback(Feedback feedback) { this.feedback = feedback; }
    public CriterionType getCriterion() { return criterion; }
    public void setCriterion(CriterionType criterion) { this.criterion = criterion; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public WeightSource getWeightSource() { return weightSource; }
    public void setWeightSource(WeightSource weightSource) { this.weightSource = weightSource; }
}


