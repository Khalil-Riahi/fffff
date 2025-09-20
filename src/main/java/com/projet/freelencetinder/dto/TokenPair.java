package com.projet.freelencetinder.dto;

public class TokenPair {

    private String accessToken;
    private String refreshToken;

    public TokenPair() {}

    public TokenPair(String access, String refresh) {
        this.accessToken  = access;
        this.refreshToken = refresh;
    }

    public String getAccessToken()  { return accessToken; }
    public void setAccessToken(String a) { this.accessToken = a; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String r) { this.refreshToken = r; }
}

