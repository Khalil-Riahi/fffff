package com.projet.freelencetinder.servcie;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealTimePushService {
    private final SimpMessagingTemplate template;

    public RealTimePushService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void notifyUser(Long userId, String type, Object payload) {
        if (template != null) {
            template.convertAndSend("/topic/user." + userId + "/" + type, payload);
        }
    }

    public void broadcastMission(Long missionId, String type, Object payload) {
        if (template != null) {
            template.convertAndSend("/topic/mission." + missionId + "/" + type, payload);
        }
    }
}


