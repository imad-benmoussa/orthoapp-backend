package com.app.orthoapp.service;

import com.app.orthoapp.dto.CompterenduPatientResponse;
import com.app.orthoapp.dto.CompterenduResponse;
import com.app.orthoapp.dto.CreateCompterenduRequest;
import com.app.orthoapp.dto.UpdateCompterenduRequest;
import com.app.orthoapp.entity.Compterendu;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.repository.CompterenduRepository;
import com.app.orthoapp.repository.PatientRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.model.fields.merge.MailMerger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompterenduService {

    private final CompterenduRepository compterenduRepository;
    private final PatientRepository patientRepository;

    public CompterenduResponse createCompterendu(CreateCompterenduRequest request) {
        Patient patient = patientRepository.findById(Long.parseLong(request.getPatientId()))
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Compterendu compterendu = new Compterendu();
        compterendu.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        compterendu.setPatient(patient);
        compterendu.setDateRedaction(LocalDate.now());
        compterendu.setContenu(request.getContenu());

        Compterendu saved = compterenduRepository.save(compterendu);

        return new CompterenduResponse(
                saved.getId(),
                saved.getDateRedaction(),
                saved.getContenu(),
                patient.getId(),
                patient.getNom(),
                patient.getPrenom()
        );
    }

    public List<CompterenduPatientResponse> getPatientsWithCompterendu() {
        List<Compterendu> compterendus = compterenduRepository.findAll();

        return compterendus.stream()
                .filter(a -> a.getPatient() != null)
                .map(a -> new CompterenduPatientResponse(
                        String.valueOf(a.getId()),
                        String.valueOf(a.getPatient().getId()),
                        a.getPatient().getNom(),
                        a.getPatient().getPrenom(),
                        a.getPatient().getNiveauScolaire(),
                        a.getPatient().getDateNaissance(),
                        a.getDateRedaction(),
                        a.getContenu()
                ))
                .toList();
    }

    public CompterenduResponse updateCompterendu(Long id, UpdateCompterenduRequest request) {
        Compterendu compterendu = compterenduRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte rendu non trouvé"));

        compterendu.setContenu(request.getContenu());
        compterenduRepository.save(compterendu);

        return new CompterenduResponse(
                compterendu.getId(),
                compterendu.getDateRedaction(),
                compterendu.getContenu(),
                compterendu.getPatient().getId(),
                compterendu.getPatient().getNom(),
                compterendu.getPatient().getPrenom()
        );
    }

    public void deleteCompterenduById(Long id) {
        if (!compterenduRepository.existsById(id)) {
            throw new RuntimeException("Compte rendu introuvable avec l'id : " + id);
        }
        compterenduRepository.deleteById(id);
    }

    public byte[] generatePdf(Map<String, String> data) throws IOException, Docx4JException {
        // Load the template
        InputStream templateStream = new ClassPathResource("templates/compterendu.docx").getInputStream();
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateStream);

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
