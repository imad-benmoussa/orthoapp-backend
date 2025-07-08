package com.app.orthoapp.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateCompterenduRequest {
    private Map<String, Object> contenu;
}
