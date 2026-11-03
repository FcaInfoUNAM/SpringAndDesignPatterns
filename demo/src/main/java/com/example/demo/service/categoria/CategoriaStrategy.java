package com.example.demo.service.categoria;

import java.util.Locale;

import com.example.demo.dto.CategoriaRequest;

public interface CategoriaStrategy {

    boolean supports(String tipo);

    void validate(CategoriaRequest request);

    static String normalizeTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
    }
}
