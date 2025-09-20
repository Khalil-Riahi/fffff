package com.projet.freelencetinder.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.projet.freelencetinder.models.Mission;
import com.projet.freelencetinder.servcie.CompetenceService;

public class CompetenceServiceTest {

    private final CompetenceService service = new CompetenceService();

    @Test
    void listAll_shouldContainJavaScript() {
        List<String> all = service.listAll();
        assertTrue(all.contains("JavaScript"));
    }

    @Test
    void toCanonical_shouldResolveAliases() {
        assertEquals("JavaScript", service.toCanonical("js"));
        assertEquals("Kubernetes", service.toCanonical("k8s"));
    }

    @Test
    void listByCategories_unionIsNonEmpty() {
        List<String> list = service.listByCategories(Set.of(Mission.Categorie.DEVELOPPEMENT_WEB, Mission.Categorie.DESIGN_GRAPHIQUE));
        assertFalse(list.isEmpty());
        assertTrue(list.contains("Angular"));
    }

    @Test
    void search_shouldFindByPrefixOrContains() {
        List<String> r1 = service.search("rea", Set.of(Mission.Categorie.DEVELOPPEMENT_WEB));
        assertTrue(r1.stream().anyMatch(s -> s.equals("React")));
        List<String> r2 = service.search("photos", null);
        assertTrue(r2.stream().anyMatch(s -> s.equals("Adobe Photoshop")));
    }
}


