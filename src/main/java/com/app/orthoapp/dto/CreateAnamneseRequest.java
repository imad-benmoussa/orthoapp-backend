package com.app.orthoapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CreateAnamneseRequest {
    private String patientId;
    private Map<String, Object> contenu;
}
