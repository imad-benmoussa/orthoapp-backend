package com.app.orthoapp.controller;

import com.app.orthoapp.dto.AnamnesePatientResponse;
import com.app.orthoapp.dto.AnamneseResponse;
import com.app.orthoapp.dto.CreateAnamneseRequest;
import com.app.orthoapp.dto.UpdateAnamneseRequest;
import com.app.orthoapp.entity.Anamnese;
import com.app.orthoapp.entity.Medecin;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.repository.AnamneseRepository;
import com.app.orthoapp.service.AnamneseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anamnese")
@RequiredArgsConstructor
public class AnamneseController {

    private final AnamneseService anamneseService;
    private final AnamneseRepository anamneseRepository;

    @PostMapping("/create")
    public AnamneseResponse createAnamnese(@RequestBody CreateAnamneseRequest request) {
        return anamneseService.createAnamnese(request);
    }

    @GetMapping("/patients")
    public List<AnamnesePatientResponse> getPatientsWithAnamnese() {
        return anamneseService.getPatientsWithAnamnese();
    }

    @PutMapping("/{id}")
    public AnamneseResponse updateAnamnese(@PathVariable Long id, @RequestBody UpdateAnamneseRequest request) {
        return anamneseService.updateAnamnese(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnamnese(@PathVariable Long id) {
        anamneseService.deleteAnamneseById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public void downloadAnamnesePdf(@PathVariable Long id, HttpServletResponse response) throws IOException, Docx4JException, JAXBException {
        // 🔍 Récupération de l'anamnèse
        Anamnese anamnese = anamneseRepository.findById(id).orElseThrow(() -> new RuntimeException("Anamnèse introuvable"));

        // 📋 Initialisation des données à injecter dans le template
        Map<String, String> data = new HashMap<>();

        // 📌 Données du patient
        Patient patient = anamnese.getPatient();
        data.put("nom", patient.getNom());
        data.put("prenom", patient.getPrenom());
        data.put("dateNaissance", String.valueOf(patient.getDateNaissance()));
        data.put("niveauScolaire", patient.getNiveauScolaire());

        // 📌 Données du medecin
        Medecin medecin = anamnese.getPatient().getMedecin();
        data.put("medecin", medecin.getNom()+" "+medecin.getPrenom());

        // 📆 Date de création
        data.put("dateCreation", anamnese.getDateCreation() != null ? anamnese.getDateCreation().toString() : "");

        // 🔁 Champs du contenu JSON
        Map<String, Object> contenu = anamnese.getContenu();
        for (String key : contenu.keySet()) {
            Object value = contenu.get(key);
            data.put(key, value != null ? value.toString() : "");
        }

        // 📄 Génération PDF
        byte[] pdfBytes = anamneseService.generatePdf(data);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=anamnese_" + id + ".pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

}
