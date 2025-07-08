package com.app.orthoapp.repository;

import com.app.orthoapp.entity.Groupeexercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupesexercicesRepository extends JpaRepository<Groupeexercice, Long> {
    List<Groupeexercice> findByMedecinId(Long medecinId);
}
