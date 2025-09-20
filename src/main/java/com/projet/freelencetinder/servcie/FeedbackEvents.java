package com.projet.freelencetinder.servcie;

import java.time.Instant;
import java.util.List;

import com.projet.freelencetinder.models.Audience;

public class FeedbackEvents {
    public record MissionClosedEvent(Long missionId) {}
    public record FeedbackSubmittedEvent(Long missionId, Long feedbackId) {}
    public record FeedbackPairReadyEvent(Long missionId) {}
    public record FeedbackAutoPublishDueEvent(Long missionId, Instant dueAt) {}
    public record FeedbackUnderReviewAutoReleaseEvent(Long feedbackId, Instant dueAt) {}
    public record FeedbackPublishedEvent(Long missionId, List<Long> publishedFeedbackIds) {}
    public record FeedbackAggregatesUpdatedEvent(Long targetId, Audience audience) {}
    public record BadgeReevaluationRequestedEvent(Long targetId, Audience audience) {}
    public record RealTimeNotificationEvent(String topic, Object payload) {}
}


