package com.smart.rh.config;

import com.smart.rh.entity.*;
import com.smart.rh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds demo data on H2 (all profiles except mysql).
 * Skipped if any user already exists.
 *
 * Demo credentials:
 *   admin@smartrh.com  / Admin@2024    (ROLE_ADMIN)
 *   rh@smartrh.com     / Rh@2024       (ROLE_RH)
 *   alice@smartrh.com  / Employee@2024 (ROLE_EMPLOYEE)
 *   bob@smartrh.com    / Employee@2024 (ROLE_EMPLOYEE)
 */
@Slf4j
@Component
@Profile("!mysql")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository          userRepository;
    private final PosteRepository         posteRepository;
    private final CompetenceRepository    competenceRepository;
    private final EmployeRepository       employeRepository;
    private final ContratRepository       contratRepository;
    private final RecrutementRepository   recrutementRepository;
    private final CandidateRepository     candidateRepository;
    private final DossierRHRepository     dossierRHRepository;
    private final ResponsableRHRepository responsableRHRepository;
    private final PasswordEncoder         passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataInitializer: data already present — skipping seed");
            return;
        }

        log.info("DataInitializer: seeding demo data...");

        // Users
        User admin = user("admin",       "admin@smartrh.com",  "Admin@2024",    Role.ROLE_ADMIN);
        User rh    = user("rhmanager",   "rh@smartrh.com",     "Rh@2024",       Role.ROLE_RH);
        User alice = user("alice.dubois","alice@smartrh.com",  "Employee@2024", Role.ROLE_EMPLOYEE);
        User bob   = user("bob.martin",  "bob@smartrh.com",    "Employee@2024", Role.ROLE_EMPLOYEE);

        // ResponsableRH
        ResponsableRH respRH = new ResponsableRH();
        respRH.setNom("Marie Dupont");
        respRH.setUser(rh);
        respRH = responsableRHRepository.save(respRH);

        // Postes
        Poste posteJava = poste("Developpeur Java Senior", "Java, Spring Boot, SQL, Docker");
        poste("Chef de Projet IT", "Gestion de projet, Agile, Communication");

        // Competences
        competence("Java",        "Expert");
        competence("Spring Boot", "Avance");
        competence("SQL",         "Intermediaire");
        competence("Docker",      "Intermediaire");
        competence("Agile/Scrum", "Avance");

        // Recrutement
        Recrutement rec = new Recrutement();
        rec.setPosteCible("Developpeur Java Senior");
        rec.setStatut("OUVERT");
        rec.setResponsable(respRH);
        rec = recrutementRepository.save(rec);

        // Candidats
        candidate("Martin",  "Jean",   "jean.martin@example.com",   rec);
        candidate("Bernard", "Sophie", "sophie.bernard@example.com", rec);

        // Employes
        Employe empAlice = employe("Dubois", "Alice", "alice@smartrh.com", "Developpeur Java Senior", posteJava, alice);
        Employe empBob   = employe("Martin", "Bob",   "bob@smartrh.com",   "Developpeur Java Senior", posteJava, bob);

        // DossierRH
        dossier(empAlice, "Alice Dubois - nee le 15/03/1992 a Paris",
                "Master Informatique - Universite Paris VI (2015)", "");
        dossier(empBob,   "Bob Martin - ne le 22/07/1990 a Lyon",
                "Licence Informatique - Universite Lyon I (2013)", "");

        // Contrats (CDI)
        contrat(empAlice, "CDI", LocalDate.of(2022, 1, 10), new BigDecimal("4500.00"));
        contrat(empBob,   "CDI", LocalDate.of(2021, 9,  1), new BigDecimal("4200.00"));

        log.info("DataInitializer: seed complete — 4 users, 2 employes, 2 contrats");
    }

    // ── Helper builders ───────────────────────────────────────────────────────

    private User user(String username, String email, String rawPw, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPw));
        u.setRole(role);
        u.setEnabled(true);
        return userRepository.save(u);
    }

    private Poste poste(String titre, String competencesRequises) {
        Poste p = new Poste();
        p.setTitre(titre);
        p.setCompetencesRequises(competencesRequises);
        return posteRepository.save(p);
    }

    private void competence(String nom, String niveau) {
        Competence c = new Competence();
        c.setNom(nom);
        c.setNiveau(niveau);
        competenceRepository.save(c);
    }

    private void candidate(String nom, String prenom, String email, Recrutement rec) {
        Candidate c = new Candidate();
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setEmail(email);
        c.setRecrutement(rec);
        candidateRepository.save(c);
    }

    private Employe employe(String nom, String prenom, String email,
                             String posteLibelle, Poste poste, User user) {
        Employe e = new Employe();
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setEmail(email);
        e.setPosteLibelle(posteLibelle);
        e.setPoste(poste);
        e.setUser(user);
        return employeRepository.save(e);
    }

    private void dossier(Employe emp, String infosPerso, String diplomes, String documents) {
        DossierRH d = new DossierRH();
        d.setEmploye(emp);
        d.setInfosPerso(infosPerso);
        d.setDiplomes(diplomes);
        d.setDocuments(documents);
        dossierRHRepository.save(d);
    }

    private void contrat(Employe emp, String type, LocalDate dateDebut, BigDecimal salaire) {
        Contrat c = new Contrat();
        c.setEmploye(emp);
        c.setType(type);
        c.setDateDebut(dateDebut);
        c.setSalaire(salaire);
        contratRepository.save(c);
    }
}
