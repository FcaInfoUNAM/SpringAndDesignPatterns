package com.example.demo.dto;

import com.example.demo.model.Image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ImagenRequest(
        @NotBlank String url,
        @NotNull @PositiveOrZero Short prioridad,
        String tamanio,
        boolean responsiva) {

    public Image toEntity() {
        return Image.builder()
                .url(url)
                .prioridad(prioridad)
                .tamanio(tamanio)
                .responsiva(responsiva)
                .build();
    }

    public void updateEntity(Image imagen) {
        imagen.setUrl(url);
        imagen.setPrioridad(prioridad);
        imagen.setTamanio(tamanio);
        imagen.setResponsiva(responsiva);
    }
}
