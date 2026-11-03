package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Categorias")
@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter 
    private String id;
    @Setter
    private String nombre;
    @Setter
    private String descripcion;
    @Setter
    private String tipo;
    @Setter
    private String icono;
    @Setter
    private String imagenNavbar;
    
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "producto_deparments",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "deparments_id")
    )
    @Builder.Default
    @Setter 
    private Set<Producto> productos = new HashSet<>();

}