package com.example.demo.dto;

import com.example.demo.model.Department;

import jakarta.validation.constraints.NotBlank;

public record CategoriaRequest(
        @NotBlank String nombre,
        String descripcion,
        String tipo,
        String icono,
        String imagenNavbar) {

    public Department toEntity() {
        return Department.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .tipo(tipo)
                .icono(icono)
                .imagenNavbar(imagenNavbar)
                .build();
    }

    public void updateEntity(Department categoria) {
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setTipo(tipo);
        categoria.setIcono(icono);
        categoria.setImagenNavbar(imagenNavbar);
    }
}
