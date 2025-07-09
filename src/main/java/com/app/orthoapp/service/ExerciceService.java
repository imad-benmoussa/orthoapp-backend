package com.app.orthoapp.service;

import com.app.orthoapp.dto.*;
import com.app.orthoapp.entity.Exercice;
import com.app.orthoapp.entity.Exercicevalide;
import com.app.orthoapp.entity.Groupeexercice;
import com.app.orthoapp.repository.ExerciceRepository;
import com.app.orthoapp.repository.ExercicevalideRepository;
import com.app.orthoapp.repository.GroupesexercicesRepository;
import com.app.orthoapp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExerciceService {
    private final ExerciceRepository exerciceRepository;
    private final GroupesexercicesRepository groupesexercicesRepository;
    private final ExercicevalideRepository exercicevalideRepository;
    private final PatientRepository patientRepository;

    public ExerciceResponse createExercice(String groupeexerciceId, String titre, MultipartFile audio, MultipartFile image) {
        Groupeexercice groupeexercice = groupesexercicesRepository.findById(Long.parseLong(groupeexerciceId))
                .orElseThrow(() -> new RuntimeException("Exercices group not found"));

        try {
            String uploadDir = "/app/uploads";
            Files.createDirectories(Paths.get(uploadDir));

            String audioFileName = UUID.randomUUID() + "_" + audio.getOriginalFilename();
            Path audioPath = Paths.get(uploadDir, audioFileName);
            Files.write(audioPath, audio.getBytes());

            Exercice exercice = new Exercice();
            exercice.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
            exercice.setGroupe(groupeexercice);
            exercice.setTitre(titre);
            exercice.setUrlAudio("/uploads/" + audioFileName);

            // ✅ Gestion facultative de l'image
            if (image != null && !image.isEmpty()) {
                String imageFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir, imageFileName);
                Files.write(imagePath, image.getBytes());
                exercice.setUrlImage("/uploads/" + imageFileName);
            } else {
                exercice.setUrlImage(null); // si pas d'image fournie
            }

            Exercice saved = exerciceRepository.save(exercice);

            return new ExerciceResponse(
                    String.valueOf(saved.getId()),
                    String.valueOf(groupeexercice.getId()),
                    saved.getTitre(),
                    saved.getUrlAudio(),
                    saved.getUrlImage()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'enregistrement des fichiers", e);
        }
    }

    public List<ExerciceResponse> getExercicesByGroupeexercices(String groupeexercicesId) {
        return exerciceRepository.findByGroupeId(Long.parseLong(groupeexercicesId))
                .stream()
                .map(exercice -> new ExerciceResponse(
                        String.valueOf(exercice.getId()),
                        String.valueOf(exercice.getGroupe()),
                        exercice.getTitre(),
                        exercice.getUrlAudio(),
                        exercice.getUrlImage()
                ))
                .toList();
    }

    public void validerExercice(String patientId, String exerciceId) {
        if (exercicevalideRepository.existsByPatientIdAndExerciceId(Long.parseLong(patientId), Long.parseLong(exerciceId))) {
            return; // déjà validé
        }
        Exercicevalide ev = new Exercicevalide();
        ev.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        ev.setPatient(patientRepository.findById(Long.parseLong(patientId)).orElseThrow());
        ev.setExercice(exerciceRepository.findById(Long.parseLong(exerciceId)).orElseThrow());
        ev.setDateValidation(LocalDate.now());
        exercicevalideRepository.save(ev);
    }

    public List<String> getExercicesValidesParPatient(String patientId) {
        List<Exercicevalide> list = exercicevalideRepository.findByPatientId(Long.parseLong(patientId));
        System.out.println("Résultats pour patientId " + patientId + " : " + list.size());
        return list.stream()
                .map(ev -> String.valueOf(ev.getExercice().getId()))
                .toList();
    }

}
