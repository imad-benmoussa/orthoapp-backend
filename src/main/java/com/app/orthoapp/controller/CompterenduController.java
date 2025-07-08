package com.app.orthoapp.controller;

import com.app.orthoapp.dto.CompterenduPatientResponse;
import com.app.orthoapp.dto.CompterenduResponse;
import com.app.orthoapp.dto.CreateCompterenduRequest;
import com.app.orthoapp.dto.UpdateCompterenduRequest;
import com.app.orthoapp.entity.Bilantrisomie;
import com.app.orthoapp.entity.Compterendu;
import com.app.orthoapp.entity.Medecin;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.repository.CompterenduRepository;
import com.app.orthoapp.service.CompterenduService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compterendu")
@RequiredArgsConstructor
public class CompterenduController {

    private final CompterenduService compterenduService;
    private final CompterenduRepository compterenduRepository;

    @PostMapping("/create")
    public CompterenduResponse createCompterendu(@RequestBody CreateCompterenduRequest request) {
        return compterenduService.createCompterendu(request);
    }

    @GetMapping("/patients")
    public List<CompterenduPatientResponse> getPatientsWithCompterendu() {
        return compterenduService.getPatientsWithCompterendu();
    }

    @PutMapping("/{id}")
    public CompterenduResponse updateCompterendu(@PathVariable Long id, @RequestBody UpdateCompterenduRequest request) {
        return compterenduService.updateCompterendu(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompterendu(@PathVariable Long id) {
        compterenduService.deleteCompterenduById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public void downloadCompterenduPdf(@PathVariable Long id, HttpServletResponse response) throws IOException, Docx4JException, JAXBException {
        // 🔍 Récupération de l'anamnèse
        Compterendu compterendu = compterenduRepository.findById(id).orElseThrow(() -> new RuntimeException("Compte rendu introuvable"));

        // 📋 Initialisation des données à injecter dans le template
        Map<String, String> data = new HashMap<>();

        // 📌 Données du patient
        Patient patient = compterendu.getPatient();
        data.put("enfant", patient.getNom()+" "+patient.getPrenom());
        data.put("dateNaissance", String.valueOf(patient.getDateNaissance()));
        data.put("niveauScolaire", patient.getNiveauScolaire());

        // 📌 Données du medecin
        Medecin medecin = compterendu.getPatient().getMedecin();
        data.put("medecin", medecin.getNom()+" "+medecin.getPrenom());

        // 📆 Date de création
        data.put("dateCreation", compterendu.getDateRedaction() != null ? compterendu.getDateRedaction().toString() : "");

        // 🔁 Champs du contenu JSON
        Map<String, Object> contenu = compterendu.getContenu();
        for (String key : contenu.keySet()) {
            Object value = contenu.get(key);
            data.put(key, value != null ? value.toString() : "");
        }

        // 📄 Génération PDF
        byte[] pdfBytes = compterenduService.generatePdf(data);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=compte_rendu_" + id + ".pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}
