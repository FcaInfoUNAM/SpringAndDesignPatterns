package com.example.demo.service.categoria;

import org.springframework.stereotype.Component;

import com.example.demo.dto.CategoriaRequest;

@Component
public class CategoriaGeneralStrategy implements CategoriaStrategy {

    @Override
    public boolean supports(String tipo) {
        return "GENERAL".equals(CategoriaStrategy.normalizeTipo(tipo));
    }

    @Override
    public void validate(CategoriaRequest request) {
    }
}
