package com.projet.freelencetinder.models;

/** État d’un livrable côté workflow client ⇆ freelance. */
public enum StatusLivrable {
    EN_ATTENTE,   // uploadé par le freelance, en attente de validation
    VALIDE,       // accepté par le client
    REJETE        // refusé par le client
}
