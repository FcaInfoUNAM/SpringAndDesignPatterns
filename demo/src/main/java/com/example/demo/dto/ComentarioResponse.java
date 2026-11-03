package com.example.demo.dto;

import com.example.demo.model.Comment;

public record ComentarioResponse(
        String id,
        String username,
        String comentario_corto,
        Short estrellas,
        String comentario_largo,
        String imagen1,
        String imagen2) {

    public static ComentarioResponse from(Comment comentario) {
        return new ComentarioResponse(
                comentario.getId(),
                comentario.getUsername(),
                comentario.getComentario_corto(),
                comentario.getEstrellas(),
                comentario.getComentario_largo(),
                comentario.getImagen1(),
                comentario.getImagen2());
    }
}
