package com.projet.freelencetinder.servcie;

import java.time.Instant;
import java.time.Duration;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.projet.freelencetinder.models.Feedback;

@Service
public class AntiAbuseLightService {

    private final TaskScheduler taskScheduler;
    private final org.springframework.context.ApplicationEventPublisher publisher;

    public AntiAbuseLightService(TaskScheduler taskScheduler,
                                 org.springframework.context.ApplicationEventPublisher publisher) {
        this.taskScheduler = taskScheduler;
        this.publisher = publisher;
    }

    public int riskScore(Feedback f) {
        int score = 0;
        if (f.getOverallRating() != null && f.getOverallRating().doubleValue() <= 1.0 && (f.getComment() == null || f.getComment().length() < 30)) {
            score += 60;
        }
        // Heuristiques légères additionnelles possibles ici
        return Math.min(score, 100);
    }

    public void scheduleAutoRelease(Long feedbackId, Instant base) {
        Instant due = base.plus(Duration.ofDays(7));
        taskScheduler.schedule(() -> publisher.publishEvent(new FeedbackEvents.FeedbackUnderReviewAutoReleaseEvent(feedbackId, due)), java.util.Date.from(due));
    }
}


