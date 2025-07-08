package com.app.orthoapp.controller;

import com.app.orthoapp.dto.CreatePatientRequest;
import com.app.orthoapp.dto.PatientResponse;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.security.JwtService;
import com.app.orthoapp.service.PatientService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final JwtService jwtService;

    // Endpoint pour créer un patient
    @PostMapping("/create")
    public PatientResponse createPatient(@RequestBody CreatePatientRequest request, @RequestHeader("Authorization") String tokenHeader) {
        // Retirer le préfixe "Bearer " du token
        String token = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;

        // Extraire le ID du médecin à partir du token JWT
        String medecinId = extractMedecinIdFromToken(token);
        Patient created = patientService.createPatient(request, Long.parseLong(medecinId));

        return new PatientResponse(
                String.valueOf(created.getId()),
                created.getNom(),
                created.getPrenom(),
                created.getUtilisateur().getEmail(),
                created.getNiveauScolaire(),
                created.getDateNaissance()
        );
    }

    @GetMapping("/all")
    public List<PatientResponse> getAllPatients(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;
        String medecinId = extractMedecinIdFromToken(token);
        return patientService.getAllPatientsByMedecin(medecinId);
    }

    private String extractMedecinIdFromToken(String token) {
        // Extraire les claims à partir du token
        Claims claims = jwtService.extractAllClaims(token);

        // Extraire l'ID du médecin des claims (ici on suppose que l'ID du médecin est stocké sous la clé "medecinId")
        return claims.get("medecinId", String.class);
    }
}
