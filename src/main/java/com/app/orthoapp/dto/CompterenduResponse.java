package com.app.orthoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class CompterenduResponse {
    private Long id;
    private LocalDate dateCreation;
    private Map<String, Object> contenu;
    private Long patientId;
    private String patientNom;
    private String patientPrenom;
}
