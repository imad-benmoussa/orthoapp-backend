package com.app.orthoapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterMedecinRequest {
    private String email;
    private String motDePasse;
    private String nom;
    private String prenom;
}
