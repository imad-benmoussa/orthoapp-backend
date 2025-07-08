package com.app.orthoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
public class CompterenduPatientResponse {
    private String compterenduId;
    private String patientId;
    private String nom;
    private String prenom;
    private String niveauScolaire;
    private LocalDate dateNaissance;
    private LocalDate dateCreation;
    Map<String, Object> contenu;
}
