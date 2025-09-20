package com.projet.freelencetinder.servcie;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RealTimeNotificationsListener {

    private final RealTimePushService push;

    public RealTimeNotificationsListener(RealTimePushService push) {
        this.push = push;
    }

    @EventListener
    public void onRealtime(FeedbackEvents.RealTimeNotificationEvent evt) {
        // Route to generic mission topic if payload contains missionId
        Object missionId = null;
        if (evt.payload() instanceof java.util.Map<?,?> map) {
            missionId = map.get("missionId");
        }
        if (missionId instanceof Long mid) {
            push.broadcastMission(mid, evt.topic(), evt.payload());
        }
    }
}


