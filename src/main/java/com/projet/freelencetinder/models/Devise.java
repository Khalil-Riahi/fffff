package com.projet.freelencetinder.models;

public enum Devise {
    TND, EUR, USD;

    public static Devise from(String value) {
        if (value == null) return TND;
        return switch (value.trim().toUpperCase()) {
            case "EUR" -> EUR;
            case "USD" -> USD;
            default -> TND;
        };
    }

    @Override
    public String toString() {
        return name();
    }
}