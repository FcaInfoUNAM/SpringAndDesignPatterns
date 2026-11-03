package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.service.categoria.CategoriaStrategy;

@Service
public class CategoriaService {

    private final List<CategoriaStrategy> strategies;

    public CategoriaService(List<CategoriaStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public void validate(CategoriaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cuerpo de la categoría es obligatorio.");
        }

        String normalizedTipo = CategoriaStrategy.normalizeTipo(request.tipo());
        CategoriaStrategy strategy = strategies.stream()
                .filter(candidate -> candidate.supports(normalizedTipo))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tipo de categoría no válido. Valores permitidos: GENERAL, ICONO y NAVBAR."));

        strategy.validate(request);
    }
}
