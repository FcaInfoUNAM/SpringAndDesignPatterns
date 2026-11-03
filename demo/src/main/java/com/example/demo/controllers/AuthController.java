package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.AppUser;
import com.example.demo.repo.appUserRepo;
import com.example.demo.service.JwtTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y emisión de JWT")
public class AuthController {

    @Autowired
    appUserRepo users;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenService jwtTokenService;

    @PostMapping("register")
    @Operation(summary = "Registrar una cuenta de usuario")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (users.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        AppUser account = users.save(new AppUser(
                request.username(),
                passwordEncoder.encode(request.password()),
                "USER"));
        return ResponseEntity.status(201)
                .body(new UserResponse(account.getId(), account.getUsername(), account.getRole()));
    }

    @PostMapping("login")
    @Operation(summary = "Iniciar sesión y obtener un JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            request.username(), request.password()));
            String token = jwtTokenService.createToken(authentication);
            return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
        } catch (AuthenticationException exception) {
            return ResponseEntity.status(401).build();
        }
    }
}
