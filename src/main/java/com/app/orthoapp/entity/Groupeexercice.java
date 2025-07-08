package com.app.orthoapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "groupeexercice")
public class Groupeexercice {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "titre")
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id")
    private Medecin medecin;

}