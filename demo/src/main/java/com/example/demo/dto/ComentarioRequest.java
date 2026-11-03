package com.example.demo.dto;

import com.example.demo.model.Comment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComentarioRequest(
        @NotBlank String username,
        @NotBlank String comentario_corto,
        @NotNull @Min(1) @Max(5) Short estrellas,
        String comentario_largo,
        String imagen1,
        String imagen2) {

    public Comment toEntity() {
        return Comment.builder()
                .username(username)
                .comentario_corto(comentario_corto)
                .estrellas(estrellas)
                .comentario_largo(comentario_largo)
                .imagen1(imagen1)
                .imagen2(imagen2)
                .build();
    }

    public void updateEntity(Comment comentario) {
        comentario.setUsername(username);
        comentario.setComentario_corto(comentario_corto);
        comentario.setEstrellas(estrellas);
        comentario.setComentario_largo(comentario_largo);
        comentario.setImagen1(imagen1);
        comentario.setImagen2(imagen2);
    }
}
