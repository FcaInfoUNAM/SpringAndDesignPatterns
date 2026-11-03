package com.example.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.AppUser;

public interface appUserRepo extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByUsername(String username);
}
