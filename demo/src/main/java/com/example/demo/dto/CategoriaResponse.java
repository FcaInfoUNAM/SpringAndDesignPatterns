package com.example.demo.dto;

import com.example.demo.model.Department;

public record CategoriaResponse(
        String id,
        String nombre,
        String descripcion,
        String tipo,
        String icono,
        String imagenNavbar) {

    public static CategoriaResponse from(Department categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getTipo(),
                categoria.getIcono(),
                categoria.getImagenNavbar());
    }
}
