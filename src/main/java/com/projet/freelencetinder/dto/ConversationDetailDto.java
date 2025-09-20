// ConversationDetailDto.java
package com.projet.freelencetinder.dto;

import java.util.List;

public class ConversationDetailDto {

    private Long conversationId;
    private Long missionId;
    private String missionTitre;

    private Long otherUserId;
    private String otherUserNomComplet;
    private String otherUserPhotoUrl;

    private List<ChatMessageResponse> messages;

    /* Constructeurs */
    public ConversationDetailDto() {}

    public ConversationDetailDto(Long conversationId,
                                 Long missionId,
                                 String missionTitre,
                                 Long otherUserId,
                                 String otherUserNomComplet,
                                 String otherUserPhotoUrl,
                                 List<ChatMessageResponse> messages) {
        this.conversationId = conversationId;
        this.missionId = missionId;
        this.missionTitre = missionTitre;
        this.otherUserId = otherUserId;
        this.otherUserNomComplet = otherUserNomComplet;
        this.otherUserPhotoUrl = otherUserPhotoUrl;
        this.messages = messages;
    }

    /* Getters / Setters */
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

    public List<ChatMessageResponse> getMessages() { return messages; }
    public void setMessages(List<ChatMessageResponse> messages) { this.messages = messages; }
}
