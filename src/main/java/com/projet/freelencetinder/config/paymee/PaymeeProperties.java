package com.projet.freelencetinder.config.paymee;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paymee")
public class PaymeeProperties {

    /* --- clés d’API --- */
    private String publicKey;
    private String privateKey;

    /* --- URLs et identifiants --- */
    private String baseUrl;     // ex. https://sandbox.paymee.tn/api
    private String returnUrl;
    private String cancelUrl;
    private String webhookUrl;
    private String vendor;      // id commerçant Paymee

    /* --- infos client Paymee v2 (obligatoires) --- */
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    /* ===== getters / setters ===== */

    public String getPublicKey()               { return publicKey; }
    public void   setPublicKey(String publicKey){ this.publicKey = publicKey; }

    public String getPrivateKey()              { return privateKey; }
    public void   setPrivateKey(String privateKey){ this.privateKey = privateKey; }

    public String getBaseUrl()                 { return baseUrl; }
    public void   setBaseUrl(String baseUrl)   { this.baseUrl = baseUrl; }

    public String getReturnUrl()               { return returnUrl; }
    public void   setReturnUrl(String returnUrl){ this.returnUrl = returnUrl; }

    public String getCancelUrl()               { return cancelUrl; }
    public void   setCancelUrl(String cancelUrl){ this.cancelUrl = cancelUrl; }

    public String getWebhookUrl()              { return webhookUrl; }
    public void   setWebhookUrl(String webhookUrl){ this.webhookUrl = webhookUrl; }

    public String getVendor()                  { return vendor; }
    public void   setVendor(String vendor)     { this.vendor = vendor; }

    public String getFirstName()               { return firstName; }
    public void   setFirstName(String firstName){ this.firstName = firstName; }

    public String getLastName()                { return lastName; }
    public void   setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getPhoneNumber()             { return phoneNumber; }
    public void   setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}