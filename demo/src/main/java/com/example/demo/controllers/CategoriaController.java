package com.example.demo.controllers;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.dto.CategoriaResponse;
import com.example.demo.model.Department;
import com.example.demo.repo.categoriaRepo;
import com.example.demo.repo.productoRepo;
import com.example.demo.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Categorías", description = "Operaciones para consultar y administrar categorías")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    private final categoriaRepo repo;

    private final productoRepo repoProducto;
    private final CategoriaService categoriaService;

    public CategoriaController(
            categoriaRepo repo,
            productoRepo repoProducto,
            CategoriaService categoriaService) {
        this.repo = repo;
        this.repoProducto = repoProducto;
        this.categoriaService = categoriaService;
    }

    @GetMapping("categorias")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar categorías")
    public ResponseEntity<List<CategoriaResponse>> findAllCategoria() {
        List<CategoriaResponse> response = repo.findAll().stream()
                .map(CategoriaResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("categoria")
    @Operation(
            summary = "Crear categoría",
            description = "tipo acepta GENERAL, ICONO o NAVBAR. ICONO requiere icono; NAVBAR requiere imagenNavbar.")
    public ResponseEntity<CategoriaResponse> addCategoria(@Valid @RequestBody CategoriaRequest request) {
        categoriaService.validate(request);

        Department saved = repo.save(request.toEntity());
        URI location = URI.create("/categoria/" + saved.getId());
        return ResponseEntity.created(location).body(CategoriaResponse.from(saved));
    }

    @PostMapping("addProductoCategoria/{productId}/{categoriaId}")
    @Operation(summary = "Asociar un producto con una categoría")
    public ResponseEntity<CategoriaResponse> addProductoCategoria(
            @PathVariable String productId,
            @PathVariable String categoriaId) {
        var producto = repoProducto.findById(productId).orElse(null);
        var categoria = repo.findById(categoriaId).orElse(null);
        if (producto == null || categoria == null) {
            return ResponseEntity.notFound().build();
        }

        categoria.getProductos().add(producto);
        producto.getDeparments().add(categoria);
        Department saved = repo.save(categoria);
        return ResponseEntity.ok(CategoriaResponse.from(saved));
    }

    @GetMapping("categoria/producto/{productId}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar categorías asociadas a un producto")
    public ResponseEntity<Set<CategoriaResponse>> getDepartmentsByProduct(@PathVariable String productId) {
        return repoProducto.findById(productId)
                .map(producto -> producto.getDeparments().stream()
                        .map(CategoriaResponse::from)
                        .collect(Collectors.toSet()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("categoria/{id}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Buscar categoría por ID")
    public ResponseEntity<CategoriaResponse> getById(@PathVariable String id) {
        return repo.findById(id)
                .map(categoria -> ResponseEntity.ok(CategoriaResponse.from(categoria)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("categoria/{id}")
    @Operation(
            summary = "Reemplazar categoría",
            description = "tipo acepta GENERAL, ICONO o NAVBAR. ICONO requiere icono; NAVBAR requiere imagenNavbar.")
    public ResponseEntity<CategoriaResponse> putDepartment(
            @PathVariable String id,
            @Valid @RequestBody CategoriaRequest request) {
        return repo.findById(id)
                .map(categoria -> {
                    categoriaService.validate(request);
                    request.updateEntity(categoria);
                    return ResponseEntity.ok(CategoriaResponse.from(repo.save(categoria)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("categoria/{id}")
    @Operation(summary = "Eliminar categoría")
    public ResponseEntity<Void> deleteCategoria(@PathVariable String id) {
        if (repo.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
