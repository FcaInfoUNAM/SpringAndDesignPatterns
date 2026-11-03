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

import com.example.demo.dto.ComentarioRequest;
import com.example.demo.dto.ComentarioResponse;
import com.example.demo.repo.comentarioRepo;
import com.example.demo.repo.productoRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Comentarios", description = "Operaciones para consultar y administrar comentarios")
@SecurityRequirement(name = "bearerAuth")
public class ComentarioController {

    @Autowired
    comentarioRepo repo;

    @Autowired
    productoRepo repoProducto;

    @GetMapping("comentarios")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar comentarios")
    public ResponseEntity<List<ComentarioResponse>> findAllComentarios() {
        List<ComentarioResponse> response = repo.findAll().stream()
                .map(ComentarioResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("comentario")
    @Operation(summary = "Crear comentario")
    public ResponseEntity<ComentarioResponse> addComentario(@Valid @RequestBody ComentarioRequest request) {
        var saved = repo.save(request.toEntity());
        URI location = URI.create("/comentario/" + saved.getId());
        return ResponseEntity.created(location).body(ComentarioResponse.from(saved));
    }

    @PostMapping("comentario/{productId}")
    @Operation(summary = "Crear comentario para un producto")
    public ResponseEntity<ComentarioResponse> addComentario(
            @Valid @RequestBody ComentarioRequest request,
            @PathVariable String productId) {
        return repoProducto.findById(productId)
                .map(producto -> {
                    var comentario = request.toEntity();
                    comentario.setProducto(producto);
                    var saved = repo.save(comentario);
                    URI location = URI.create("/comentario/" + saved.getId());
                    return ResponseEntity.created(location).body(ComentarioResponse.from(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("comentarios/{productId}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar comentarios de un producto")
    public ResponseEntity<Set<ComentarioResponse>> getCommentsByProduct(@PathVariable String productId) {
        return repoProducto.findById(productId)
                .map(producto -> producto.getComentarios().stream()
                        .map(ComentarioResponse::from)
                        .collect(Collectors.toSet()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("comentario/{id}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Buscar comentario por ID")
    public ResponseEntity<ComentarioResponse> getById(@PathVariable String id) {
        return repo.findById(id)
                .map(comentario -> ResponseEntity.ok(ComentarioResponse.from(comentario)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("comentario/{id}")
    @Operation(summary = "Reemplazar comentario")
    public ResponseEntity<ComentarioResponse> putComentario(
            @PathVariable String id,
            @Valid @RequestBody ComentarioRequest request) {
        return repo.findById(id)
                .map(comentario -> {
                    request.updateEntity(comentario);
                    return ResponseEntity.ok(ComentarioResponse.from(repo.save(comentario)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("comentario/{id}")
    @Operation(summary = "Eliminar comentario")
    public ResponseEntity<Void> deleteComentario(@PathVariable String id) {
        if (repo.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
