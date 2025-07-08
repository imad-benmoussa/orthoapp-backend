package com.app.orthoapp.repository;

import com.app.orthoapp.entity.Exercicevalide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExercicevalideRepository extends JpaRepository<Exercicevalide, Long> {
    boolean existsByPatientIdAndExerciceId(Long patientId, Long exerciceId);
    List<Exercicevalide> findByPatientId(Long patientId);
}
