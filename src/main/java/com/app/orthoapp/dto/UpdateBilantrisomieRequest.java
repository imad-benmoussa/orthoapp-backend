package com.app.orthoapp.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateBilantrisomieRequest {
    private Map<String, Object> contenu;
}
