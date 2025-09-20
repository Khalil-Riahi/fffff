package com.projet.freelencetinder.dto;

public class RefeshTokenRequest {

    private String refreshToken;

    public RefeshTokenRequest() {}
    public RefeshTokenRequest(String r) { this.refreshToken = r; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String r) { this.refreshToken = r; }
}
