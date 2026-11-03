package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Table(name="comentarios")
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter 
    private String id;
    @Setter
    private String username;
    @Setter
    private String comentario_corto;
    @Setter
    private Short estrellas;
    @Setter
    private String comentario_largo;
    @Setter
    private String imagen1;
    @Setter
    private String imagen2;
    
    @JsonBackReference
    @ManyToOne(fetch=FetchType.LAZY)
    @Setter
    private Producto producto;

}