package com.app.orthoapp.controller;

import com.app.orthoapp.dto.BilantrisomiePatientResponse;
import com.app.orthoapp.dto.BilantrisomieResponse;
import com.app.orthoapp.dto.CreateBilantrisomieRequest;
import com.app.orthoapp.dto.UpdateBilantrisomieRequest;
import com.app.orthoapp.entity.Anamnese;
import com.app.orthoapp.entity.Bilantrisomie;
import com.app.orthoapp.entity.Medecin;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.repository.BilantrisomieRepository;
import com.app.orthoapp.service.BilantrisomieService;
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
@RequestMapping("/api/bilantrisomie")
@RequiredArgsConstructor
public class BilantrisomieController {

    private final BilantrisomieService bilantrisomieService;
    private final BilantrisomieRepository bilantrisomieRepository;

    @PostMapping("/create")
    public BilantrisomieResponse createBilantrisomie(@RequestBody CreateBilantrisomieRequest request) {
        return bilantrisomieService.createBilantrisomie(request);
    }

    @GetMapping("/patients")
    public List<BilantrisomiePatientResponse> getPatientsWithBilantrisomie() {
        return bilantrisomieService.getPatientsWithBilantrisomie();
    }

    @PutMapping("/{id}")
    public BilantrisomieResponse updateBilantrisomie(@PathVariable Long id, @RequestBody UpdateBilantrisomieRequest request) {
        return bilantrisomieService.updateBilantrisomie(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBilantrisomie(@PathVariable Long id) {
        bilantrisomieService.deleteBilantrisomieById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public void downloadBilantrisomiePdf(@PathVariable Long id, HttpServletResponse response) throws IOException, Docx4JException, JAXBException {
        // 🔍 Récupération de l'anamnèse
        Bilantrisomie bilantrisomie = bilantrisomieRepository.findById(id).orElseThrow(() -> new RuntimeException("Bilan trisomie introuvable"));

        // 📋 Initialisation des données à injecter dans le template
        Map<String, String> data = new HashMap<>();

        // 📌 Données du patient
        Patient patient = bilantrisomie.getPatient();
        data.put("enfant", patient.getNom()+" "+patient.getPrenom());
        data.put("dateNaissance", String.valueOf(patient.getDateNaissance()));

        // 📆 Date de création
        data.put("dateCreation", bilantrisomie.getDateCreation() != null ? bilantrisomie.getDateCreation().toString() : "");

        // 🔁 Champs du contenu JSON
        Map<String, Object> contenu = bilantrisomie.getContenu();
        for (String key : contenu.keySet()) {
            Object value = contenu.get(key);
            data.put(key, value != null ? value.toString() : "");
        }

        // 📄 Génération PDF
        byte[] pdfBytes = bilantrisomieService.generatePdf(data);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=bilan_trisomie_" + id + ".pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}
