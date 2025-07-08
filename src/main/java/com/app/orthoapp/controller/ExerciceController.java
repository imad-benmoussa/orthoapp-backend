package com.app.orthoapp.controller;

import com.app.orthoapp.dto.*;
import com.app.orthoapp.service.ExerciceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/exercices")
@RequiredArgsConstructor
public class ExerciceController {

    private final ExerciceService exerciceService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ExerciceResponse createExercice(
            @RequestParam("groupeexerciceId") String groupeexerciceId,
            @RequestParam("titre") String titre,
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        return exerciceService.createExercice(groupeexerciceId, titre, audio, image);
    }

    @GetMapping("/{id}/all")
    public List<ExerciceResponse> getExercicesByGroupeexercices(@PathVariable String id) {
        return exerciceService.getExercicesByGroupeexercices(id);
    }

    @PostMapping("/valider")
    public void validerExercice(@RequestParam String patientId, @RequestParam String exerciceId) {
        exerciceService.validerExercice(patientId, exerciceId);
    }

    @GetMapping("/valides")
    public List<String> getExercicesValidesParPatient(@RequestParam String patientId) {
        return exerciceService.getExercicesValidesParPatient(patientId);
    }

}
