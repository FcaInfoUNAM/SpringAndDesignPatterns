package com.example.demo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoRequest(
        @NotBlank String nombre,
        @NotBlank String descripcion,
        @NotBlank String lote,
        @NotNull @PositiveOrZero Float precioUnitario,
        @NotNull LocalDate fechaCaducidad) {
}
