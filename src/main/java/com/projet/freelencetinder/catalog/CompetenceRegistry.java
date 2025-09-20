package com.projet.freelencetinder.catalog;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import com.projet.freelencetinder.models.Mission;

/**
 * Catalogue canonique des compétences par catégorie (MVP en mémoire).
 * - Clés canoniques en anglais (ex: "JavaScript", "Spring Boot").
 * - Alias courants mappés vers les clés canoniques (ex: "js" -> "JavaScript").
 * - Utilitaires de recherche, normalisation et union par catégories.
 */
public final class CompetenceRegistry {

    public static final Map<Mission.Categorie, List<String>> COMPETENCES_PAR_CATEGORIE;
    public static final List<String> TOUTES_LES_COMPETENCES;
    public static final Map<String, Set<Mission.Categorie>> CATEGORIES_PAR_COMPETENCE;

    /** alias normalises (lower/without accents) -> clé canonique */
    public static final Map<String, String> ALIAS_TO_CANONICAL;

    /** Labels optionnels FR/AR pour i18n légère: skill -> { lang -> label } */
    public static final Map<String, Map<String, String>> LABELS;

    static {
        Map<Mission.Categorie, List<String>> tmp = new EnumMap<>(Mission.Categorie.class);

        tmp.put(Mission.Categorie.DEVELOPPEMENT_WEB, List.of(
                "HTML5", "CSS3", "Sass/SCSS", "Tailwind CSS", "Bootstrap",
                "JavaScript", "TypeScript",
                "Angular", "React", "Vue.js", "Svelte",
                "Next.js", "Nuxt.js",
                "Node.js", "Express.js", "NestJS",
                "Java", "Spring Boot", "JPA/Hibernate",
                "Python", "Django", "Flask", "FastAPI",
                "PHP", "Laravel", "Symfony",
                "REST API", "GraphQL", "WebSocket",
                "JWT", "OAuth2", "Keycloak",
                "PostgreSQL", "MySQL/MariaDB", "MongoDB", "Redis", "Elasticsearch",
                "Docker", "Kubernetes", "Nginx", "CI/CD (GitHub Actions, GitLab CI)",
                "Testing (Jest, Cypress, Playwright, JUnit)", "Accessibility (a11y)", "Technical SEO",
                "i18n FR/AR", "SSR/SSG", "Microservices",
                "Paymee (TND)", "Flouci (TND)", "Stripe", "PayPal"
        ));

        tmp.put(Mission.Categorie.DEVELOPPEMENT_MOBILE, List.of(
                "Kotlin (Android)", "Java (Android)", "Jetpack Compose",
                "Swift (iOS)", "SwiftUI",
                "Flutter", "Dart",
                "React Native", "Expo",
                "Ionic", "Capacitor",
                "Kotlin Multiplatform",
                "Firebase (Auth, Firestore, FCM, Crashlytics)", "Realm", "SQLite",
                "REST API mobile", "GraphQL mobile",
                "Push Notifications", "Deep Linking", "Background Services",
                "Payment SDK (Paymee, Flouci, Stripe)", "In-App Purchases",
                "App Signing/Release", "Play Console", "App Store Connect",
                "ASO", "Testing (JUnit, XCTest, Mockito)", "CI/CD mobile (Fastlane)"
        ));

        tmp.put(Mission.Categorie.DESIGN_GRAPHIQUE, List.of(
                "Branding", "Logo Design", "Charte Graphique", "Identité Visuelle",
                "UI Design", "UX Design", "Wireframing", "Prototypage",
                "Design System", "Web/Mobile Design",
                "Adobe Photoshop", "Adobe Illustrator", "Adobe InDesign",
                "Figma", "Sketch", "Adobe XD",
                "Motion Design", "After Effects", "Lottie",
                "3D (Blender)", "Illustration", "Infographie",
                "Print (CMJN, Prépresse)", "Packaging", "Typographie", "Color Theory",
                "Social Media Banners", "Templates Canva"
        ));

        tmp.put(Mission.Categorie.REDACTION_CONTENU, List.of(
                "Rédaction Web", "SEO Copywriting", "Content Strategy",
                "Articles de Blog", "Landing Pages", "Fiches Produits",
                "UX Writing", "Microcopy",
                "Social Media Copy (FB/IG/TikTok/LinkedIn)", "Scripts Vidéo",
                "Newsletter", "Email Marketing",
                "Relecture/Correction", "Réécriture", "Synthèse",
                "FR natif", "AR standard", "Darija TN", "EN pro",
                "Localisation FR/AR/EN", "Transcréation",
                "SEO Tools (Ahrefs, SEMrush, SurferSEO)", "WordPress", "Notion"
        ));

        tmp.put(Mission.Categorie.MARKETING_DIGITAL, List.of(
                "Stratégie Digitale", "Content Marketing", "Calendrier éditorial",
                "SEO On-Page", "SEO Off-Page", "Technical SEO",
                "Google Ads (Search/Display)", "YouTube Ads", "Facebook/Instagram Ads", "TikTok Ads", "LinkedIn Ads",
                "Social Media Management", "Community Management", "Influence Marketing",
                "Analytics (GA4)", "Google Tag Manager", "Looker Studio", "Conversion Tracking (Pixel/Tag)",
                "CRO (A/B testing)", "Emailing/Automation (Mailchimp, Brevo)", "CRM (HubSpot)",
                "ASO", "E-commerce (Shopify, WooCommerce)",
                "SMS Marketing", "WhatsApp Business", "Growth Hacking"
        ));

        tmp.put(Mission.Categorie.VIDEO_MONTAGE, List.of(
                "Montage Vidéo", "Storytelling",
                "Adobe Premiere Pro", "Final Cut Pro", "DaVinci Resolve",
                "After Effects", "Motion Graphics", "Transitions", "Lower Thirds",
                "Color Grading", "Étalonnage", "Correction Colorimétrique",
                "Sound Design", "Mixage Audio", "Denoise",
                "Sous-titres FR/AR/EN", "SRT", "Kinetic Typography",
                "Formats Verticaux (Reels/TikTok/Shorts)", "YouTube Packaging (Titre/Vignette/Chapitres)",
                "Live Streaming (OBS)", "Export Multi-réseaux"
        ));

        tmp.put(Mission.Categorie.TRADUCTION, List.of(
                "FR → AR", "AR → FR", "FR ↔ EN", "AR ↔ EN",
                "Traduction Générale", "Technique (IT/Ingénierie)", "Marketing",
                "Juridique", "Financière", "Médicale",
                "Localisation App/Site", "Transcréation Publicitaire", "Sous-titres/SRT",
                "Relecture/Correction", "Adaptation Culturelle TN",
                "CAT Tools (SDL Trados, memoQ, Smartcat)", "QA Linguistique",
                "Terminologie/Glossaires", "Style Guides"
        ));

        tmp.put(Mission.Categorie.SUPPORT_TECHNIQUE, List.of(
                "Helpdesk L1/L2", "ITIL Basics", "SLA Management",
                "Ticketing (Zendesk, Freshdesk, Jira Service Management)",
                "Windows Admin", "macOS", "Linux Basics",
                "Active Directory", "Microsoft 365", "Google Workspace",
                "Réseaux (TCP/IP, DNS, DHCP)", "Firewall/NAT", "VPN",
                "Sécurité (Antivirus/EDR)", "Backup/Restore",
                "Remote Support (AnyDesk, TeamViewer)", "MDM (Intune)",
                "Scripting (PowerShell, Bash)", "Asset Management",
                "Documentation/KB", "Relation Client"
        ));

        tmp.put(Mission.Categorie.CONSULTING, List.of(
                "Product Management", "Discovery/Delivery", "Roadmapping",
                "Agile/Scrum/Kanban", "PO/SM",
                "Business Analysis", "BPMN", "UML",
                "Data Analysis (Excel/Sheets, SQL, Power BI)", "KPI/OKR",
                "Go-to-Market", "Pricing", "Benchmark/Études",
                "DevOps/Cloud Strategy", "Cloud (AWS/GCP/Azure) Architecture",
                "Cybersecurity Basics (ISO 27001, OWASP)", "RGPD",
                "ERP (Odoo, SAP)", "CRM (Salesforce, HubSpot)",
                "Change Management", "Formation/Coaching"
        ));

        tmp.put(Mission.Categorie.AUTRE, List.of(
                "Data Science (Python, Pandas, scikit-learn)", "NLP/LLM (Prompt Engineering, RAG)",
                "MLOps (MLflow, Docker)", "Computer Vision (OpenCV)",
                "BI (Power BI, Tableau)", "ETL (Airflow)", "BigQuery",
                "QA Test Manuel", "QA Automatisation (Selenium, Cypress, Playwright)",
                "No-Code/Low-Code (Bubble, Webflow, Make/Zapier)",
                "Blockchain (Solidity)", "Smart Contracts", "Web3",
                "Game Dev (Unity, Unreal)", "AR/VR (Three.js, WebXR)",
                "Audio Production (Audition, Ableton)", "Architecture (AutoCAD, Revit)"
        ));

        COMPETENCES_PAR_CATEGORIE = Collections.unmodifiableMap(tmp);

        // Build inverse map and full list
        Map<String, Set<Mission.Categorie>> inverse = new LinkedHashMap<>();
        for (Map.Entry<Mission.Categorie, List<String>> e : tmp.entrySet()) {
            for (String skill : e.getValue()) {
                inverse.computeIfAbsent(skill, k -> new LinkedHashSet<>()).add(e.getKey());
            }
        }
        CATEGORIES_PAR_COMPETENCE = Collections.unmodifiableMap(inverse);
        TOUTES_LES_COMPETENCES = Collections.unmodifiableList(
                inverse.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList())
        );

        // Aliases (normalized -> canonical)
        Map<String, String> aliases = new HashMap<>();
        aliases.put(nf("js"), "JavaScript");
        aliases.put(nf("ts"), "TypeScript");
        aliases.put(nf("rn"), "React Native");
        aliases.put(nf("ps"), "Adobe Photoshop");
        aliases.put(nf("ai"), "Adobe Illustrator");
        aliases.put(nf("ux"), "UX Design");
        aliases.put(nf("seo"), "Technical SEO");
        aliases.put(nf("ga4"), "Analytics (GA4)");
        aliases.put(nf("k8s"), "Kubernetes");
        aliases.put(nf("jwt"), "JWT");
        aliases.put(nf("oauth"), "OAuth2");
        aliases.put(nf("hibernate"), "JPA/Hibernate");
        aliases.put(nf("postgres"), "PostgreSQL");
        aliases.put(nf("mysql"), "MySQL/MariaDB");
        aliases.put(nf("ci cd"), "CI/CD (GitHub Actions, GitLab CI)");
        ALIAS_TO_CANONICAL = Collections.unmodifiableMap(aliases);

        // Minimal labels FR/AR (extensible)
        Map<String, Map<String, String>> labels = new HashMap<>();
        putLabel(labels, "JavaScript", Map.of("fr", "JavaScript", "ar", "جافاسكريبت"));
        putLabel(labels, "Spring Boot", Map.of("fr", "Spring Boot", "ar", "سبرينغ بوت"));
        putLabel(labels, "Angular", Map.of("fr", "Angular", "ar", "أنغولار"));
        putLabel(labels, "React", Map.of("fr", "React", "ar", "ريأكت"));
        putLabel(labels, "Adobe Photoshop", Map.of("fr", "Adobe Photoshop", "ar", "أدوبي فوتوشوب"));
        putLabel(labels, "Kubernetes", Map.of("fr", "Kubernetes", "ar", "كوبيرنيتس"));
        LABELS = Collections.unmodifiableMap(labels);
    }

    private CompetenceRegistry() {}

    private static void putLabel(Map<String, Map<String, String>> labels, String key, Map<String, String> value) {
        labels.put(key, new HashMap<>(value));
    }

    /** Normalized form: lowercase and without accents/diacritics and trimmed */
    private static String nf(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase(Locale.ROOT);
        String n = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n;
    }

    /**
     * Union dédupliquée des compétences pour un ensemble de catégories.
     */
    public static List<String> getByCategories(Set<Mission.Categorie> cats) {
        if (cats == null || cats.isEmpty()) return TOUTES_LES_COMPETENCES;
        return cats.stream()
                .filter(COMPETENCES_PAR_CATEGORIE::containsKey)
                .flatMap(c -> COMPETENCES_PAR_CATEGORIE.get(c).stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Recherche prefix/contains (case/accent insensitive), limitée à 100.
     * Si cats est fourni, on restreint la recherche à l'union de ces catégories.
     */
    public static List<String> searchByPrefix(String q, Set<Mission.Categorie> cats) {
        if (q == null || q.trim().isEmpty()) return List.of();
        String nq = nf(q);
        List<String> base = (cats == null || cats.isEmpty()) ? TOUTES_LES_COMPETENCES : getByCategories(cats);

        // Two-stage: startsWith then contains
        List<String> starts = base.stream()
                .filter(s -> nf(s).startsWith(nq))
                .limit(100)
                .collect(Collectors.toList());
        if (starts.size() >= 100) return starts;

        Set<String> already = new HashSet<>(starts);
        for (String s : base) {
            if (already.size() >= 100) break;
            if (!already.contains(s) && nf(s).contains(nq)) {
                already.add(s);
                starts.add(s);
            }
        }
        return starts;
    }

    /**
     * Renvoie la clé canonique si on la connaît (via alias/équivalence), sinon null.
     */
    public static String toCanonicalOrNull(String input) {
        if (input == null) return null;
        String normalized = nf(input);

        // alias direct
        if (ALIAS_TO_CANONICAL.containsKey(normalized)) {
            return ALIAS_TO_CANONICAL.get(normalized);
        }

        // match par normalisation sur les clés canoniques
        for (String canonical : TOUTES_LES_COMPETENCES) {
            if (nf(canonical).equals(normalized)) return canonical;
        }
        return null;
    }

    /**
     * Normalise une entrée (trim, lower, accents) – principalement pour recherche.
     */
    public static String normalize(String input) {
        return nf(input);
    }

    /** Catégories associées à une compétence canonique. */
    public static Set<Mission.Categorie> categoriesOf(String canonicalSkill) {
        Set<Mission.Categorie> cats = CATEGORIES_PAR_COMPETENCE.get(canonicalSkill);
        return cats == null ? Set.of() : cats;
    }
}


