package com.app.orthoapp.repository;

import com.app.orthoapp.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByMedecinId(Long medecinId);
}
