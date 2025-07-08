package com.app.orthoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PatientResponse {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String niveauScolaire;
    private LocalDate dateNaissance;
}
