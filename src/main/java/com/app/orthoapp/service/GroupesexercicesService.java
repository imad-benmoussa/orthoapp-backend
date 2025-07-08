package com.app.orthoapp.service;

import com.app.orthoapp.dto.CreateGroupeexercicesRequest;
import com.app.orthoapp.dto.GroupeProgressResponse;
import com.app.orthoapp.dto.GroupesexercicesResponse;
import com.app.orthoapp.entity.Exercice;
import com.app.orthoapp.entity.Groupeexercice;
import com.app.orthoapp.entity.Medecin;
import com.app.orthoapp.repository.ExerciceRepository;
import com.app.orthoapp.repository.ExercicevalideRepository;
import com.app.orthoapp.repository.GroupesexercicesRepository;
import com.app.orthoapp.repository.MedecinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupesexercicesService {
    private final MedecinRepository medecinRepository;
    private final GroupesexercicesRepository groupesexercicesRepository;
    private final ExerciceRepository exerciceRepository;
    private final ExercicevalideRepository exercicevalideRepository;

    public Groupeexercice createGroupeexercices(CreateGroupeexercicesRequest request, Long medecinId) {

        Long GroupeexercicesId = Math.abs(UUID.randomUUID().getMostSignificantBits());

        Groupeexercice groupeexercice = new Groupeexercice();
        groupeexercice.setId(GroupeexercicesId);
        groupeexercice.setTitre(request.getTitre());

        Medecin medecin = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        groupeexercice.setMedecin(medecin);

        return groupesexercicesRepository.save(groupeexercice);

    }

    public List<GroupesexercicesResponse> getAllGroupesexercicesByMedecin(Long medecinId) {
        return groupesexercicesRepository.findByMedecinId(medecinId)
                .stream()
                .map(groupeexercice -> new GroupesexercicesResponse(
                        String.valueOf(groupeexercice.getId()),
                        groupeexercice.getTitre()
                ))
                .toList();
    }

    public List<GroupeProgressResponse> getGroupesWithProgressByPatient(String patientId) {
        List<Groupeexercice> groupes = groupesexercicesRepository.findAll();

        return groupes.stream().map(groupe -> {
            List<Exercice> exercices = exerciceRepository.findByGroupeId(groupe.getId());
            int total = exercices.size();
            int valides = (int) exercices.stream().filter(exo ->
                    exercicevalideRepository.existsByPatientIdAndExerciceId(Long.parseLong(patientId), exo.getId())
            ).count();
            int pourcentage = total == 0 ? 0 : (valides * 100) / total;

            return new GroupeProgressResponse(
                    String.valueOf(groupe.getId()),
                    groupe.getTitre(),
                    total,
                    valides,
                    pourcentage
            );
        }).toList();
    }
}
