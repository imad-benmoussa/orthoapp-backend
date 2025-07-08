package com.app.orthoapp.repository;

import com.app.orthoapp.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    List<Exercice> findByGroupeId(Long groupeexercicesId);
}
