package com.example.demo.service.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;

@Component
public class CategoriaNavbarStrategy implements CategoriaStrategy {

    @Override
    public boolean supports(String tipo) {
        return "NAVBAR".equals(CategoriaStrategy.normalizeTipo(tipo));
    }

    @Override
    public void validate(CategoriaRequest request) {
        if (!StringUtils.hasText(request.imagenNavbar())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo NAVBAR requiere un campo imagenNavbar no vacío.");
        }
    }
}
