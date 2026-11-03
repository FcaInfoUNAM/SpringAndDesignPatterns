package com.example.demo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductoPatchRequest(
        @Size(min = 1) String nombre,
        @Size(min = 1) String descripcion,
        @Size(min = 1) String lote,
        @PositiveOrZero Float precioUnitario,
        LocalDate fechaCaducidad) {

    public boolean isEmpty() {
        return nombre == null
                && descripcion == null
                && lote == null
                && precioUnitario == null
                && fechaCaducidad == null;
    }
}
