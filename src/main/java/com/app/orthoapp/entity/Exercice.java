package com.app.orthoapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "exercice")
public class Exercice {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    private Groupeexercice groupe;

    @Column(name = "titre")
    private String titre;

    @Column(name = "url_audio", length = 500)
    private String urlAudio;

    @Column(name = "url_image", length = 500)
    private String urlImage;

}