package com.example.demo.dto;

import java.time.LocalDate;

import com.example.demo.model.Producto;

public record ProductoResponse(
        String id,
        String nombre,
        String descripcion,
        String lote,
        float precioUnitario,
        LocalDate fechaCaducidad) {

    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getLote(),
                producto.getPrecioUnitario(),
                producto.getFechaCaducidad());
    }
}
