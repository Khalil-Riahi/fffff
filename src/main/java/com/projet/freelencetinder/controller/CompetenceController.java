package com.projet.freelencetinder.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.servcie.CompetenceService;
import com.projet.freelencetinder.catalog.CompetenceRegistry;

@RestController
@RequestMapping("/api/competences")
public class CompetenceController {

    private final CompetenceService competenceService;

    @Autowired
    public CompetenceController(CompetenceService competenceService) {
        this.competenceService = competenceService;
    }

    /**
     * GET /api/competences → toutes les compétences (triées)
     */
    @GetMapping
    public ResponseEntity<List<String>> listAll() {
        return ResponseEntity.ok(competenceService.listAll());
    }

    /**
     * GET /api/competences/by-categories?cat=A&cat=B → union dédupliquée
     */
    @GetMapping("/by-categories")
    public ResponseEntity<List<String>> listByCategories(@RequestParam(name = "cat") List<Mission.Categorie> cats) {
        Set<Mission.Categorie> set = new LinkedHashSet<>(cats);
        return ResponseEntity.ok(competenceService.listByCategories(set));
    }

    /**
     * GET /api/competences/search?q=react&cat=DEVELOPPEMENT_WEB
     */
    @GetMapping("/search")
    public ResponseEntity<List<String>> search(@RequestParam(name = "q") String q,
                                               @RequestParam(name = "cat", required = false) List<Mission.Categorie> cats) {
        Set<Mission.Categorie> set = (cats == null || cats.isEmpty()) ? null : new LinkedHashSet<>(cats);
        return ResponseEntity.ok(competenceService.search(q, set));
    }

    /**
     * (Optionnel) Labels i18n légers: /api/competences/labels?lang=fr&cat=...
     */
    @GetMapping("/labels")
    public ResponseEntity<List<Map<String, String>>> labels(@RequestParam(name = "lang", defaultValue = "en") String lang,
                                                            @RequestParam(name = "cat", required = false) List<Mission.Categorie> cats) {
        Set<Mission.Categorie> set = (cats == null || cats.isEmpty()) ? null : new LinkedHashSet<>(cats);
        List<String> base = (set == null) ? competenceService.listAll() : competenceService.listByCategories(set);
        List<Map<String, String>> result = base.stream().map(key -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("key", key);
            String label = Optional.ofNullable(CompetenceRegistry.LABELS.get(key))
                    .map(map -> map.getOrDefault(lang.toLowerCase(Locale.ROOT), key))
                    .orElse(key);
            m.put("label", label);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}


