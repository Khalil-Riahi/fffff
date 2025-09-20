// ChatMessageResponse.java
package com.projet.freelencetinder.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String type;            // "TEXT" | "LINK" | "FILE"
    private String fileUrl;
    private String fileType;
    private LocalDateTime timestamp;
    private boolean seen;

    /* Constructeurs */
    public ChatMessageResponse() {}

    public ChatMessageResponse(Long id,
                               Long conversationId,
                               Long senderId,
                               Long receiverId,
                               String content,
                               String type,
                               String fileUrl,
                               String fileType,
                               LocalDateTime timestamp,
                               boolean seen) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    /* Getters / Setters */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}
