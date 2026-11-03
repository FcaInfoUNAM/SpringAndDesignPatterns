package com.example.demo.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProductoPatchRequest;
import com.example.demo.dto.ProductoRequest;
import com.example.demo.dto.ProductoResponse;
import com.example.demo.model.Producto;
import com.example.demo.repo.productoRepo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Productos", description = "Operaciones para consultar y administrar productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    @Autowired
    productoRepo repo;

    @GetMapping("productos")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Listar productos")
    public ResponseEntity<List<ProductoResponse>> findAllProductos() {
        List<ProductoResponse> response = repo.findAll().stream()
                .map(ProductoResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("producto")
    @Operation(summary = "Crear producto")
    public ResponseEntity<ProductoResponse> addProducto(@Valid @RequestBody ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .lote(request.lote())
                .precioUnitario(request.precioUnitario())
                .fechaCaducidad(request.fechaCaducidad())
                .build();

        Producto saved = repo.save(producto);
        URI location = URI.create("/producto/" + saved.getId());
        return ResponseEntity.created(location).body(ProductoResponse.from(saved));
    }

    @GetMapping("producto/{id}")
    @PreAuthorize("permitAll()")
    @SecurityRequirements
    @Operation(summary = "Buscar producto por ID")
    public ResponseEntity<ProductoResponse> getById(@PathVariable String id) {
        return repo.findById(id)
                .map(producto -> ResponseEntity.ok(ProductoResponse.from(producto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("producto/{id}")
    @Operation(summary = "Reemplazar producto")
    public ResponseEntity<ProductoResponse> putProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductoRequest request) {
        return repo.findById(id)
                .map(producto -> {
                    updateProducto(producto, request);
                    return ResponseEntity.ok(ProductoResponse.from(repo.save(producto)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("producto/{id}")
    @Operation(summary = "Actualizar parcialmente un producto")
    public ResponseEntity<ProductoResponse> patchProducto(
            @PathVariable String id,
            @Valid @RequestBody ProductoPatchRequest patch) {
        if (patch.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return repo.findById(id)
                .map(producto -> {
                    applyPatch(producto, patch);
                    return ResponseEntity.ok(ProductoResponse.from(repo.save(producto)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("producto/{id}")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
        if (repo.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void updateProducto(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setLote(request.lote());
        producto.setPrecioUnitario(request.precioUnitario());
        producto.setFechaCaducidad(request.fechaCaducidad());
    }

    private void applyPatch(Producto producto, ProductoPatchRequest patch) {
        if (patch.nombre() != null) {
            producto.setNombre(patch.nombre());
        }
        if (patch.descripcion() != null) {
            producto.setDescripcion(patch.descripcion());
        }
        if (patch.lote() != null) {
            producto.setLote(patch.lote());
        }
        if (patch.precioUnitario() != null) {
            producto.setPrecioUnitario(patch.precioUnitario());
        }
        if (patch.fechaCaducidad() != null) {
            producto.setFechaCaducidad(patch.fechaCaducidad());
        }
    }
}
