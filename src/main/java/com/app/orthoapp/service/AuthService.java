package com.app.orthoapp.service;

import com.app.orthoapp.dto.JwtResponse;
import com.app.orthoapp.dto.LoginRequest;
import com.app.orthoapp.dto.RegisterMedecinRequest;
import com.app.orthoapp.entity.Medecin;
import com.app.orthoapp.entity.Utilisateur;
import com.app.orthoapp.enums.Role;
import com.app.orthoapp.repository.MedecinRepository;
import com.app.orthoapp.repository.UtilisateurRepository;
import com.app.orthoapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponse register(RegisterMedecinRequest request) {

        Long userId = Math.abs(UUID.randomUUID().getMostSignificantBits());

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(userId);
        utilisateur.setEmail(request.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        utilisateur.setRole(Role.MEDECIN);
        utilisateurRepository.save(utilisateur);

        Medecin medecin = new Medecin();
        medecin.setId(userId); // même ID que l'utilisateur
        medecin.setNom(request.getNom());
        medecin.setPrenom(request.getPrenom());
        medecin.setUtilisateur(utilisateur);
        medecinRepository.save(medecin);

        String jwt = jwtService.generateToken(utilisateur, String.valueOf(medecin.getId()));
        return new JwtResponse(jwt, utilisateur.getRole().name());
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String jwt = jwtService.generateToken(utilisateur, String.valueOf(utilisateur.getId()));
        return new JwtResponse(jwt, utilisateur.getRole().name());
    }
}
