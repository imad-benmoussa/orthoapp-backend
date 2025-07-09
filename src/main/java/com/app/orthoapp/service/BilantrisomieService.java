package com.app.orthoapp.service;

import com.app.orthoapp.dto.BilantrisomiePatientResponse;
import com.app.orthoapp.dto.BilantrisomieResponse;
import com.app.orthoapp.dto.CreateBilantrisomieRequest;
import com.app.orthoapp.dto.UpdateBilantrisomieRequest;
import com.app.orthoapp.entity.Bilantrisomie;
import com.app.orthoapp.entity.Patient;
import com.app.orthoapp.repository.BilantrisomieRepository;
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
public class BilantrisomieService {

    private final BilantrisomieRepository bilantrisomieRepository;
    private final PatientRepository patientRepository;

    public BilantrisomieResponse createBilantrisomie(CreateBilantrisomieRequest request) {
        Patient patient = patientRepository.findById(Long.parseLong(request.getPatientId()))
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Bilantrisomie bilan = new Bilantrisomie();
        bilan.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        bilan.setPatient(patient);
        bilan.setDateCreation(LocalDate.now());
        bilan.setContenu(request.getContenu());

        Bilantrisomie saved = bilantrisomieRepository.save(bilan);

        return new BilantrisomieResponse(
                saved.getId(),
                saved.getDateCreation(),
                saved.getContenu(),
                patient.getId(),
                patient.getNom(),
                patient.getPrenom()
        );
    }

    public List<BilantrisomiePatientResponse> getPatientsWithBilantrisomie() {
        List<Bilantrisomie> bilans = bilantrisomieRepository.findAll();

        return bilans.stream()
                .filter(b -> b.getPatient() != null)
                .map(b -> new BilantrisomiePatientResponse(
                        String.valueOf(b.getId()),
                        String.valueOf(b.getPatient().getId()),
                        b.getPatient().getNom(),
                        b.getPatient().getPrenom(),
                        b.getPatient().getNiveauScolaire(),
                        b.getPatient().getDateNaissance(),
                        b.getDateCreation(),
                        b.getContenu()
                ))
                .toList();
    }

    public BilantrisomieResponse updateBilantrisomie(Long id, UpdateBilantrisomieRequest request) {
        Bilantrisomie bilan = bilantrisomieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bilan trisomique non trouvé"));

        bilan.setContenu(request.getContenu());
        bilantrisomieRepository.save(bilan);

        return new BilantrisomieResponse(
                bilan.getId(),
                bilan.getDateCreation(),
                bilan.getContenu(),
                bilan.getPatient().getId(),
                bilan.getPatient().getNom(),
                bilan.getPatient().getPrenom()
        );
    }

    public void deleteBilantrisomieById(Long id) {
        if (!bilantrisomieRepository.existsById(id)) {
            throw new RuntimeException("Bilan trisomique introuvable avec l'id : " + id);
        }
        bilantrisomieRepository.deleteById(id);
    }

    public byte[] generatePdf(Map<String, String> data) throws IOException, Docx4JException {
        // Load the template
        InputStream templateStream = new ClassPathResource("templates/bilan.docx").getInputStream();
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
