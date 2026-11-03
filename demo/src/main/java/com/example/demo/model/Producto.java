package com.example.demo.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="producto")
@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter 
    private String id;

    @Setter
    private String nombre;
  
    @Setter
    private String descripcion;
    @Setter
    private String lote;
    @Setter
    private float precioUnitario;
    @Setter
    private LocalDate fechaCaducidad;

    @JsonManagedReference
    @OneToMany(mappedBy = "producto",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    @Setter
    private Set<Comment> comentarios = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "producto",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    @Setter 
    private Set<Image> imagenes = new HashSet<>();

    @JsonBackReference
    @ManyToMany(mappedBy = "productos", fetch = FetchType.EAGER)
    @Builder.Default
    @Setter 
    private Set<Department> deparments = new HashSet<>();
}
