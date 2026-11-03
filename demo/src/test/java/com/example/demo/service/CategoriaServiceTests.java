package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.service.categoria.CategoriaGeneralStrategy;
import com.example.demo.service.categoria.CategoriaIconoStrategy;
import com.example.demo.service.categoria.CategoriaNavbarStrategy;
import com.example.demo.service.categoria.CategoriaStrategy;

class CategoriaServiceTests {

    private final CategoriaService service = new CategoriaService(List.of(
            new CategoriaGeneralStrategy(),
            new CategoriaIconoStrategy(),
            new CategoriaNavbarStrategy()));

    @Test
    void acceptsGeneralWithoutSpecificFields() {
        assertDoesNotThrow(() -> service.validate(request("GENERAL", null, null)));
    }

    @Test
    void acceptsTypeWithWhitespaceAndDifferentCase() {
        assertDoesNotThrow(() -> service.validate(request("  gEnErAl  ", null, null)));
    }

    @Test
    void requiresIconForIconType() {
        assertDoesNotThrow(() -> service.validate(request("ICONO", "fa-star", null)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request("ICONO", " ", null)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "El tipo ICONO requiere un campo icono no vacío.",
                exception.getReason());
    }

    @Test
    void requiresNavbarImageForNavbarType() {
        assertDoesNotThrow(() -> service.validate(request("NAVBAR", null, "/nav.png")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request("NAVBAR", null, "")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "El tipo NAVBAR requiere un campo imagenNavbar no vacío.",
                exception.getReason());
    }

    @Test
    void rejectsUnknownAndNullTypes() {
        ResponseStatusException unknown = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request("OTRO", null, null)));
        ResponseStatusException missing = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request(null, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, missing.getStatusCode());
        assertTrue(unknown.getReason().contains("GENERAL, ICONO y NAVBAR"));
    }

    @Test
    void supportsIsCaseInsensitiveAndNullSafe() {
        CategoriaStrategy strategy = new CategoriaIconoStrategy();

        assertTrue(strategy.supports(" icono "));
        assertFalse(strategy.supports(null));
    }

    private CategoriaRequest request(String tipo, String icono, String imagenNavbar) {
        return new CategoriaRequest("Categoría", null, tipo, icono, imagenNavbar);
    }
}
