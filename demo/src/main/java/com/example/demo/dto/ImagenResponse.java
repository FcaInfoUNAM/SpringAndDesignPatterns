package com.example.demo.dto;

import com.example.demo.model.Image;

public record ImagenResponse(
        String id,
        String url,
        Short prioridad,
        String tamanio,
        boolean responsiva) {

    public static ImagenResponse from(Image imagen) {
        return new ImagenResponse(
                imagen.getId(),
                imagen.getUrl(),
                imagen.getPrioridad(),
                imagen.getTamanio(),
                imagen.isResponsiva());
    }
}
