package com.app.orthoapp.service;

import com.app.orthoapp.dto.AnamnesePatientResponse;
import com.app.orthoapp.dto.AnamneseResponse;
import com.app.orthoapp.dto.CreateAnamneseRequest;
import com.app.orthoapp.dto.UpdateAnamneseRequest;
import com.app.orthoapp.entity.Anamnese;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.repository.AnamneseRepository;
import com.app.orthoapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.model.fields.merge.MailMerger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnamneseService {
    private final AnamneseRepository anamneseRepository;
    private final PatientRepository patientRepository;

    public AnamneseResponse createAnamnese(CreateAnamneseRequest request) {
        Patient patient = patientRepository.findById(Long.parseLong(request.getPatientId()))
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Anamnese anamnese = new Anamnese();
        anamnese.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        anamnese.setPatient(patient);
        anamnese.setDateCreation(LocalDate.now());
        anamnese.setContenu(request.getContenu());

        Anamnese saved = anamneseRepository.save(anamnese);

        return new AnamneseResponse(
                saved.getId(),
                saved.getDateCreation(),
                saved.getContenu(),
                patient.getId(),
                patient.getNom(),
                patient.getPrenom()
        );
    }

    public List<AnamnesePatientResponse> getPatientsWithAnamnese() {
        List<Anamnese> anamneses = anamneseRepository.findAll();

        return anamneses.stream()
                .filter(a -> a.getPatient() != null)
                .map(a -> new AnamnesePatientResponse(
                        String.valueOf(a.getId()),
                        String.valueOf(a.getPatient().getId()),
                        a.getPatient().getNom(),
                        a.getPatient().getPrenom(),
                        a.getPatient().getNiveauScolaire(),
                        a.getPatient().getDateNaissance(),
                        a.getDateCreation(),
                        a.getContenu()
                ))
                .toList();
    }

    public AnamneseResponse updateAnamnese(Long id, UpdateAnamneseRequest request) {
        Anamnese anamnese = anamneseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anamnèse non trouvée"));

        anamnese.setContenu(request.getContenu());
        anamneseRepository.save(anamnese);

        return new AnamneseResponse(
                anamnese.getId(),
                anamnese.getDateCreation(),
                anamnese.getContenu(),
                anamnese.getPatient().getId(),
                anamnese.getPatient().getNom(),
                anamnese.getPatient().getPrenom()
        );
    }

    public void deleteAnamneseById(Long id) {
        if (!anamneseRepository.existsById(id)) {
            throw new RuntimeException("Anamnèse introuvable avec l'id : " + id);
        }
        anamneseRepository.deleteById(id);
    }

    public byte[] generatePdf(Map<String, String> data) throws IOException, Docx4JException {
        // Load the template
        File template = new ClassPathResource("templates/anamnese.docx").getFile();
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(template);

        // Convertir Map<String, String> en Map<DataFieldName, String>
        Map<DataFieldName, String> mergedData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            mergedData.put(new DataFieldName(entry.getKey()), entry.getValue());
        }

        // Remplacement des champs dans le DOCX
        MailMerger.setMERGEFIELDInOutput(MailMerger.OutputField.REMOVED);
        MailMerger.performMerge(wordMLPackage, mergedData, true);

        // Préparation du flux de sortie
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Création des paramètres FO
            FOSettings foSettings = Docx4J.createFOSettings();
            foSettings.setWmlPackage(wordMLPackage);
            foSettings.setApacheFopMime("application/pdf");

            // Initialisation du moteur FOP
            FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(foSettings, fopFactory);
            // 🔥 PAS de setFOUserAgent, ce n’est pas nécessaire

            // Conversion DOCX vers PDF
            Docx4J.toPDF(wordMLPackage, outputStream);

        } catch (FOPException | Docx4JException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return outputStream.toByteArray();
    }

}
