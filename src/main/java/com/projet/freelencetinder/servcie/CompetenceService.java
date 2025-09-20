package com.projet.freelencetinder.servcie;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.projet.freelencetinder.catalog.CompetenceRegistry;
import com.projet.freelencetinder.models.Mission;

@Service
public class CompetenceService {

    private static final Logger log = LoggerFactory.getLogger(CompetenceService.class);

    public List<String> listAll() {
        return CompetenceRegistry.TOUTES_LES_COMPETENCES;
    }

    public List<String> listByCategories(Set<Mission.Categorie> categories) {
        return CompetenceRegistry.getByCategories(categories);
    }

    public List<String> search(String query, @Nullable Set<Mission.Categorie> categories) {
        return CompetenceRegistry.searchByPrefix(query, categories);
    }

    public boolean isKnown(String skill) {
        return toCanonicalOrNull(skill) != null;
    }

    public String toCanonical(String input) {
        if (input == null) return null;
        String c = toCanonicalOrNull(input);
        return c != null ? c : input;
    }

    public String toCanonicalOrNull(String input) {
        return CompetenceRegistry.toCanonicalOrNull(input);
    }

    public Set<Mission.Categorie> categoriesOf(String canonicalSkill) {
        return CompetenceRegistry.categoriesOf(canonicalSkill);
    }

    /** Normalise un set en clés canoniques, conserve les inconnues telles quelles. */
    public Set<String> normalizeSet(Set<String> raw) {
        if (raw == null) return null;
        Set<String> out = raw.stream()
                .filter(Objects::nonNull)
                .map(this::toCanonical)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> unknowns = out.stream()
                .filter(s -> !isKnown(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!unknowns.isEmpty()) {
            log.info("[Competences] Unknown skills kept as-is: {}", unknowns);
        }
        return out;
    }
}


