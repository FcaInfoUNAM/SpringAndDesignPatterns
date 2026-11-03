package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.model.AppUser;
import com.example.demo.model.Producto;
import com.example.demo.repo.appUserRepo;
import com.example.demo.repo.categoriaRepo;
import com.example.demo.repo.comentarioRepo;
import com.example.demo.repo.imagenRepo;
import com.example.demo.repo.productoRepo;
import com.example.demo.service.JwtTokenService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityAndDtoTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private productoRepo productoRepo;

    @MockBean
    private comentarioRepo comentarioRepo;

    @MockBean
    private categoriaRepo categoriaRepo;

    @MockBean
    private imagenRepo imagenRepo;

    @MockBean
    private appUserRepo appUserRepo;

    @BeforeEach
    void resetRepositories() {
        reset(productoRepo, comentarioRepo, categoriaRepo, imagenRepo, appUserRepo);
    }

    @Test
    void everyGetEndpointIsPublicButProductAndCategoryWritesRequireAdmin() throws Exception {
        when(productoRepo.findAll()).thenReturn(List.of());
        when(comentarioRepo.findAll()).thenReturn(List.of());
        when(categoriaRepo.findAll()).thenReturn(List.of());
        when(imagenRepo.findAll()).thenReturn(List.of());
        when(productoRepo.findById(anyString())).thenReturn(Optional.empty());
        when(comentarioRepo.findById(anyString())).thenReturn(Optional.empty());
        when(categoriaRepo.findById(anyString())).thenReturn(Optional.empty());
        when(imagenRepo.findById(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/producto/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/comentarios/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/comentario/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/categoria/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/categoria/producto/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/imagenes"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/imagenes/producto/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/imagen/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/producto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/producto")
                        .header("Authorization", bearerToken("usuario", "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Producto",
                                  "descripcion": "Descripción",
                                  "lote": "L-1",
                                  "precioUnitario": 10.0,
                                  "fechaCaducidad": "2027-01-01"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCanAccessCommentsAndImages() throws Exception {
        when(imagenRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/imagenes")
                        .header("Authorization", bearerToken("usuario", "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void registrationAndLoginIssueJwtThatAuthenticatesRequests() throws Exception {
        Map<String, AppUser> accounts = new HashMap<>();
        when(appUserRepo.findByUsername(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(accounts.get(invocation.getArgument(0))));
        when(appUserRepo.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser account = invocation.getArgument(0);
            account.setId(UUID.randomUUID().toString());
            accounts.put(account.getUsername(), account);
            return account;
        });
        when(comentarioRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"estudiante","password":"password-segura-123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"estudiante","password":"password-segura-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String token = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(get("/imagenes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void invalidLoginReturnsUnauthorized() throws Exception {
        AppUser account = new AppUser("estudiante", passwordEncoder.encode("correct-password"), "USER");
        when(appUserRepo.findByUsername("estudiante")).thenReturn(Optional.of(account));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"estudiante","password":"incorrect-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidProductDtoBeforeSaving() throws Exception {
        mockMvc.perform(post("/producto")
                        .header("Authorization", bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "descripcion": "Producto",
                                  "lote": "L-1",
                                  "precioUnitario": 10.0,
                                  "fechaCaducidad": "2027-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoRepo);
    }

    @Test
    void patchChangesOnlyFieldsIncludedInTheDto() throws Exception {
        Producto producto = Producto.builder()
                .id("producto-1")
                .nombre("Nombre original")
                .descripcion("Descripción original")
                .lote("L-1")
                .precioUnitario(10.0f)
                .build();
        when(productoRepo.findById("producto-1")).thenReturn(Optional.of(producto));
        when(productoRepo.save(any(Producto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/producto/producto-1")
                        .header("Authorization", bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Nombre actualizado"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre actualizado"))
                .andExpect(jsonPath("$.descripcion").value("Descripción original"))
                .andExpect(jsonPath("$.precioUnitario").value(10.0));
    }

    @Test
    void rejectsEmptyPatch() throws Exception {
        mockMvc.perform(patch("/producto/producto-1")
                        .header("Authorization", bearerToken("admin", "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productoRepo);
    }

    @Test
    void publishesOpenApiWithBearerJwtSchemeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
    }

    private String bearerToken(String username, String role) {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return "Bearer " + jwtTokenService.createToken(authentication);
    }
}
