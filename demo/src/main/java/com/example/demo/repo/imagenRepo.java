package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Image;

public interface imagenRepo extends JpaRepository <Image,String>{

}
