// SendMessageRequest.java
package com.projet.freelencetinder.dto;

public class SendMessageRequest {

    private Long conversationId;
    private String content;
    private String type;     // "TEXT" | "LINK" | "FILE"
    private String fileUrl;
    private String fileType;

    /* Constructeurs */
    public SendMessageRequest() {}

    public SendMessageRequest(Long conversationId, String content,
                              String type, String fileUrl, String fileType) {
        this.conversationId = conversationId;
        this.content = content;
        this.type = type;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    /* Getters / Setters */
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
