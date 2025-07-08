package com.app.orthoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExerciceResponse {
    private String id;
    private String groupeexerciceId;
    private String titre;
    private String urlAudio;
    private String urlImage;
}
