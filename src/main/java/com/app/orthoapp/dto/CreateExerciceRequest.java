package com.app.orthoapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExerciceRequest {
    private String groupeexerciceId;
    private String titre;
    private String urlAudio;
    private String urlImage;
}
