package com.example.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ImagenRequest;
import com.example.demo.dto.ImagenResponse;
import com.example.demo.repo.imagenRepo;
import com.example.demo.repo.productoRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Imágenes", description = "Operaciones para consultar y administrar imágenes")
@SecurityRequirement(name = "bearerAuth")
public class ImagenController {

    @Autowired
    imagenRepo repo;

    @Autowired
    productoRepo repoProducto;

    @GetMapping("imagenes")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar imágenes")
    public ResponseEntity<List<ImagenResponse>> findAllImagenes() {
        List<ImagenResponse> response = repo.findAll().stream()
                .map(ImagenResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("imagen")
    @Operation(summary = "Crear imagen")
    public ResponseEntity<ImagenResponse> addImagen(@Valid @RequestBody ImagenRequest request) {
        var saved = repo.save(request.toEntity());
        URI location = URI.create("/imagen/" + saved.getId());
        return ResponseEntity.created(location).body(ImagenResponse.from(saved));
    }

    @PostMapping("imagen/{productId}")
    @Operation(summary = "Crear imagen para un producto")
    public ResponseEntity<ImagenResponse> addImagen(
            @Valid @RequestBody ImagenRequest request,
            @PathVariable String productId) {
        return repoProducto.findById(productId)
                .map(producto -> {
                    var imagen = request.toEntity();
                    imagen.setProducto(producto);
                    var saved = repo.save(imagen);
                    URI location = URI.create("/imagen/" + saved.getId());
                    return ResponseEntity.created(location).body(ImagenResponse.from(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("imagenes/producto/{productId}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar imágenes de un producto")
    public ResponseEntity<Set<ImagenResponse>> getImagenesByProduct(@PathVariable String productId) {
        return repoProducto.findById(productId)
                .map(producto -> producto.getImagenes().stream()
                        .map(ImagenResponse::from)
                        .collect(Collectors.toSet()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("imagen/{id}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Buscar imagen por ID")
    public ResponseEntity<ImagenResponse> getById(@PathVariable String id) {
        return repo.findById(id)
                .map(imagen -> ResponseEntity.ok(ImagenResponse.from(imagen)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("imagen/{id}")
    @Operation(summary = "Reemplazar imagen")
    public ResponseEntity<ImagenResponse> putImagen(
            @PathVariable String id,
            @Valid @RequestBody ImagenRequest request) {
        return repo.findById(id)
                .map(imagen -> {
                    request.updateEntity(imagen);
                    return ResponseEntity.ok(ImagenResponse.from(repo.save(imagen)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("imagen/{id}")
    @Operation(summary = "Eliminar imagen")
    public ResponseEntity<Void> deleteImagen(@PathVariable String id) {
        if (repo.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
