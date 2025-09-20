package com.projet.freelencetinder.dto;

import jakarta.validation.constraints.*;

public class LoginRequest {

    @NotBlank @Email   private String email;
    @NotBlank          private String password;

    public LoginRequest() {}
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail()    { return email; }
    public void setEmail(String e) { this.email = e; }

    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
}
