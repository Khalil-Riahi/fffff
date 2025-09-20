package com.projet.freelencetinder.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.projet.freelencetinder.models.Audience;
import com.projet.freelencetinder.models.CriterionType;
import com.projet.freelencetinder.models.FeedbackRole;

public class FeedbackDTOs {

    public static class ScoreItem {
        @NotNull(message = "Le critère est requis")
        public CriterionType criterion;
        
        @NotNull(message = "Le score est requis")
        @Min(value = 1, message = "Le score doit être au minimum 1")
        @Max(value = 5, message = "Le score doit être au maximum 5")
        public Integer score; // wrapper, pas int !
    }

    public static class FeedbackCreateRequestDTO {
        public Long missionId;
        public Long targetId;
        public FeedbackRole role;
        public List<@Valid ScoreItem> scores;
        public String comment;
        public String idempotencyKey;
    }

    public static class FeedbackResponseDTO {
        public Long id;
        public Long missionId;
        public Long issuerId;
        public Long targetId;
        public FeedbackRole role;
        public BigDecimal overallRating;
        public String comment;
        public String language;
        public Instant submittedAt;
        public Instant publishedAt;
    }

    public static class FeedbackListQueryDTO {
        public Long targetId;
        public Audience audience;
        public Instant periodStart;
        public Instant periodEnd;
        public Integer minRating;
        public Boolean hasText;
        public int page = 0;
        public int size = 10;
        public String sort = "publishedAt,DESC";
    }

    public static class FeedbackSummaryDTO {
        public Long targetId;
        public BigDecimal avg;
        public int count;
        public BigDecimal decayedAvg;
        public Map<Integer, Integer> distribution;
        public List<String> topKeywords;
    }

    public static class FeedbackWindowDTO {
        public Instant openedAt;
        public Instant expiresAt;
        public boolean clientSubmitted;
        public boolean freelancerSubmitted;
        public boolean doubleBlind;
        public Instant autoPublishedAt;
    }

    public static class FeedbackEligibilityDTO {
        public boolean eligible;
        public String reason;
        public Instant deadline;
    }

    public static class FeedbackUpdateRequestDTO {
        public String comment;
        public List<@Valid ScoreItem> scores;
        public String idempotencyKey;
    }
}


