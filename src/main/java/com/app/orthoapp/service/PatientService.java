package com.app.orthoapp.service;

import com.app.orthoapp.dto.CreatePatientRequest;
import com.app.orthoapp.dto.PatientResponse;
import com.app.orthoapp.entity.Medecin;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.entity.Utilisateur;
import com.app.orthoapp.enums.Role;
import com.app.orthoapp.repository.MedecinRepository;
import com.app.orthoapp.repository.PatientRepository;
import com.app.orthoapp.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final UtilisateurRepository utilisateurRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final PasswordEncoder passwordEncoder;

    public Patient createPatient(CreatePatientRequest request, Long medecinId) {
        // Vérifier si l'email est déjà pris
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Long userId = Math.abs(UUID.randomUUID().getMostSignificantBits());

        // Créer l'utilisateur pour le patient
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse())); // N'oublie pas de hasher le mot de passe
        utilisateur.setRole(Role.PATIENT); // Associe le rôle "PATIENT"
        utilisateurRepository.save(utilisateur);

        // Créer le patient
        Patient patient = new Patient();
        patient.setId(userId);
        patient.setNom(request.getNom());
        patient.setPrenom(request.getPrenom());
        patient.setDateNaissance(LocalDate.parse(request.getDateNaissance()));
        patient.setNiveauScolaire(request.getNiveauScolaire());

        // Associer le patient au médecin
        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        patient.setMedecin(medecin);
        patient.setUtilisateur(utilisateur);

        return patientRepository.save(patient);
    }

    public List<PatientResponse> getAllPatientsByMedecin(String medecinId) {
        return patientRepository.findByMedecinId(Long.parseLong(medecinId))
                .stream()
                .map(patient -> new PatientResponse(
                        String.valueOf(patient.getId()),
                        patient.getNom(),
                        patient.getPrenom(),
                        patient.getUtilisateur().getEmail(),
                        patient.getNiveauScolaire(),
                        patient.getDateNaissance()
                ))
                .toList();
    }
}
