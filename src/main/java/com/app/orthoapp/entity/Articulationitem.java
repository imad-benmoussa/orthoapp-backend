package com.app.orthoapp.entity;

import com.app.orthoapp.enums.ArticulationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "articulationitem")
public class Articulationitem {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bilan_id")
    private Bilanarticulation bilan;

    @Column(name = "texte_associe")
    private String texteAssocie;

    @Column(name = "point_articulation_errone")
    private Boolean pointArticulationErrone;
    @Column(name = "trouble_articulatoire_audible")
    private Boolean troubleArticulatoireAudible;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ArticulationType type;
}