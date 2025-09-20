// src/main/java/com/projet/freelencetinder/exception/PaymeeApiException.java

package com.projet.freelencetinder.exception;

public class PaymeeApiException extends RuntimeException {
    public PaymeeApiException(String message) {
        super("Paymee API error: " + message);
    }
}
