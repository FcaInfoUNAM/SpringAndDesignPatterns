package com.example.demo.service.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;

@Component
public class CategoriaIconoStrategy implements CategoriaStrategy {

    @Override
    public boolean supports(String tipo) {
        return "ICONO".equals(CategoriaStrategy.normalizeTipo(tipo));
    }

    @Override
    public void validate(CategoriaRequest request) {
        if (!StringUtils.hasText(request.icono())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo ICONO requiere un campo icono no vacío.");
        }
    }
}
