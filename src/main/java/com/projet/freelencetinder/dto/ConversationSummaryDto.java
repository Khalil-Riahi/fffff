// ConversationSummaryDto.java
package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;

public class ConversationSummaryDto {

	private Long conversationId;
    private Long missionId;
    private String missionTitre;

    private Long otherUserId;
    private String otherUserNomComplet;
    private String otherUserPhotoUrl;

    private String lastMessagePreview;
    private String lastMessageType;    // TEXT / LINK / FILE
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;

    /* Constructeurs */
    public ConversationSummaryDto() {}

    public ConversationSummaryDto(Long conversationId,
                                  Long missionId,
                                  String missionTitre,
                                  Long otherUserId,
                                  String otherUserNomComplet,
                                  String otherUserPhotoUrl,
                                  String lastMessagePreview,
                                  String lastMessageType,
                                  LocalDateTime lastMessageAt,
                                  Integer unreadCount) {
        this.conversationId = conversationId;
        this.missionId = missionId;
        this.missionTitre = missionTitre;
        this.otherUserId = otherUserId;
        this.otherUserNomComplet = otherUserNomComplet;
        this.otherUserPhotoUrl = otherUserPhotoUrl;
        this.lastMessagePreview = lastMessagePreview;
        this.lastMessageType = lastMessageType;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    /* ----- Getters / Setters ----- */
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getMissionId() { return missionId; }
    public void setMissionId(Long missionId) { this.missionId = missionId; }

    public String getMissionTitre() { return missionTitre; }
    public void setMissionTitre(String missionTitre) { this.missionTitre = missionTitre; }

    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }

    public String getOtherUserNomComplet() { return otherUserNomComplet; }
    public void setOtherUserNomComplet(String otherUserNomComplet) {
        this.otherUserNomComplet = otherUserNomComplet;
    }

    public String getOtherUserPhotoUrl() { return otherUserPhotoUrl; }
    public void setOtherUserPhotoUrl(String otherUserPhotoUrl) {
        this.otherUserPhotoUrl = otherUserPhotoUrl;
    }

    public String getLastMessagePreview() { return lastMessagePreview; }
    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }

    public String getLastMessageType() { return lastMessageType; }
    public void setLastMessageType(String lastMessageType) { this.lastMessageType = lastMessageType; }
}
