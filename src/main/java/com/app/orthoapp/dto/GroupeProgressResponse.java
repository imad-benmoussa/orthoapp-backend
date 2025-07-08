package com.app.orthoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupeProgressResponse {
    private String id;
    private String titre;
    private int totalExercices;
    private int exercicesValides;
    private int progressionPourcentage;
}
