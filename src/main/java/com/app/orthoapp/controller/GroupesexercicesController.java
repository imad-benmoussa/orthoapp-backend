package com.app.orthoapp.controller;

import com.app.orthoapp.dto.CreateGroupeexercicesRequest;
import com.app.orthoapp.dto.GroupeProgressResponse;
import com.app.orthoapp.dto.GroupesexercicesResponse;
import com.app.orthoapp.entity.Groupeexercice;
import com.app.orthoapp.security.JwtService;
import com.app.orthoapp.service.GroupesexercicesService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groupesexercices")
@RequiredArgsConstructor
public class GroupesexercicesController {
    private final JwtService jwtService;
    private final GroupesexercicesService groupesexercicesService;

    // Endpoint pour créer un groupe des exercices
    @PostMapping("/create")
    public GroupesexercicesResponse createGroupeexercices(@RequestBody CreateGroupeexercicesRequest request, @RequestHeader("Authorization") String tokenHeader) {
        // Retirer le préfixe "Bearer " du token
        String token = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;

        // Extraire le ID du médecin à partir du token JWT
        String medecinId = extractMedecinIdFromToken(token);
        Groupeexercice created = groupesexercicesService.createGroupeexercices(request, Long.parseLong(medecinId));

        return new GroupesexercicesResponse(
                String.valueOf(created.getId()),
                created.getTitre()
        );
    }

    @GetMapping("/all")
    public List<GroupesexercicesResponse> getAllGroupesexercices(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;
        String medecinId = extractMedecinIdFromToken(token);
        return groupesexercicesService.getAllGroupesexercicesByMedecin(Long.parseLong(medecinId));
    }

    @GetMapping("/progress")
    public List<GroupeProgressResponse> getGroupesWithProgress(@RequestParam String patientId) {
        return groupesexercicesService.getGroupesWithProgressByPatient(patientId);
    }

    private String extractMedecinIdFromToken(String token) {
        // Extraire les claims à partir du token
        Claims claims = jwtService.extractAllClaims(token);

        // Extraire l'ID du médecin des claims (ici on suppose que l'ID du médecin est stocké sous la clé "medecinId")
        return claims.get("medecinId", String.class);
    }
}
