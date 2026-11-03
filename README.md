# Ejercicio: aplica Strategy para validar categorías en Spring Boot

## Contexto

El proyecto ya administra categorías (`Department`) mediante
`CategoriaController`. La entidad y su DTO incluyen `tipo`, `icono` e
`imagenNavbar`; por ahora, la API acepta esos valores sin reglas diferentes
por tipo de categoría.

En este ejercicio vas a separar esas reglas en estrategias intercambiables
usando el patrón **Strategy**. La meta es que agregar un nuevo tipo no requiera
añadir otro bloque `if` o `switch` al controlador.

La referencia conceptual es el catálogo de
[patrones de diseño de Refactoring.Guru](https://refactoring.guru/design-patterns/catalog)
y su explicación de
[Strategy](https://refactoring.guru/design-patterns/strategy). En ese catálogo,
Strategy es un patrón de comportamiento: encapsula una familia de algoritmos
en clases distintas para poder seleccionarlos e intercambiarlos. Aquí, cada
estrategia encapsula las reglas de validación de una categoría.

> **Alcance:** trabaja en el proyecto `demo/`. No cambies las relaciones de
> base de datos, la autenticación JWT ni los endpoints de otros recursos.
> Todos los GET deben seguir siendo públicos y las escrituras de categorías
> deben seguir requiriendo `ADMIN`.

## Objetivos de aprendizaje

Al terminar podrás:

1. Identificar cuándo una clase concentra reglas que varían según un tipo.
2. Separar esas reglas en implementaciones de una interfaz común.
3. Registrar las estrategias como beans de Spring y seleccionarlas mediante
   inyección de dependencias.
4. Probar cada estrategia y verificar la integración con una API REST.

## Comportamiento que vas a construir

La API acepta estos valores de `tipo` (sin distinguir mayúsculas y
minúsculas, y quitando espacios al inicio y final):

| Tipo | Regla específica |
|---|---|
| `GENERAL` | No requiere `icono` ni `imagenNavbar`. |
| `ICONO` | Requiere `icono` no vacío. |
| `NAVBAR` | Requiere `imagenNavbar` no vacía. |
| Cualquier otro valor, incluido `null` | Se rechaza como solicitud inválida. |

Se conservan las reglas generales del DTO: `nombre` es obligatorio.
Una categoría inválida debe responder `400 Bad Request`; una categoría válida
debe conservar los códigos HTTP actuales:

- `POST /categoria`: `201 Created` y encabezado `Location`.
- `PUT /categoria/{id}`: `200 OK`, o `404 Not Found` si no existe.
- Los GET de categorías siguen respondiendo sin token.
- Las escrituras de categorías siguen requiriendo `ADMIN`.

## Antes de empezar

Desde `demo/`, ejecuta:

```bash
./mvnw test
```

Revisa estos archivos para ubicar la funcionalidad existente:

- `src/main/java/com/example/demo/controllers/CategoriaController.java`
- `src/main/java/com/example/demo/dto/CategoriaRequest.java`
- `src/main/java/com/example/demo/dto/CategoriaResponse.java`
- `src/main/java/com/example/demo/model/Department.java`
- `src/main/java/com/example/demo/SecurityConfig.java`
- `src/test/java/com/example/demo/ApiSecurityAndDtoTests.java`

`tipo` actualmente es un `String` sin restricciones. El controlador guarda y
actualiza categorías usando el DTO, pero no selecciona una regla de negocio
según `tipo`.

## Diseño que debes implementar

Crea una interfaz común `CategoriaStrategy`. Cada implementación debe:

1. Indicar si puede atender un valor de `tipo`.
2. Validar los campos específicos de ese tipo.

Registra cada estrategia como un componente Spring (`@Component`). Un servicio
de categorías recibirá `List<CategoriaStrategy>` por constructor, encontrará
la estrategia compatible y delegará la validación. El controlador llamará al
servicio antes de guardar o actualizar.

Flujo esperado:

```text
HTTP request
    -> CategoriaController
        -> CategoriaService
            -> estrategia compatible con tipo
                -> valida campos específicos
        -> repositorio guarda la categoría válida
```

El patrón está aplicado cuando el controlador y el servicio no contienen un
`switch` con reglas específicas para `GENERAL`, `ICONO` y `NAVBAR`. La selección
es responsabilidad de la estrategia compatible; la regla de cada tipo vive
en su propia clase.

## Pasos

### 1. Crea el contrato de estrategia

Crea:

```text
src/main/java/com/example/demo/service/categoria/CategoriaStrategy.java
```

Define un contrato con operaciones equivalentes a:

```java
boolean supports(String tipo);
void validate(CategoriaRequest request);
```

Puedes usar nombres en español si mantienes consistencia. No pongas reglas
particulares de cada categoría en la interfaz.

### 2. Implementa las estrategias concretas

Crea las implementaciones dentro de:

```text
src/main/java/com/example/demo/service/categoria/
```

Implementa una clase Spring por tipo:

- `CategoriaGeneralStrategy`: acepta `GENERAL`.
- `CategoriaIconoStrategy`: acepta `ICONO` y exige `icono` no vacío.
- `CategoriaNavbarStrategy`: acepta `NAVBAR` y exige `imagenNavbar` no vacía.

Normaliza el tipo para que `general`, ` GENERAL ` y `GENERAL` seleccionen la
misma estrategia. Si un campo obligatorio para una estrategia falta o está
vacío, genera un error de validación claro; no lo ignores ni lo sustituyas por
un valor exitoso por defecto.

### 3. Crea el servicio selector

Crea:

```text
src/main/java/com/example/demo/service/CategoriaService.java
```

El servicio debe:

1. Recibir `List<CategoriaStrategy>` por constructor.
2. Buscar la estrategia que soporte el `tipo` recibido.
3. Responder con error de cliente (`400`) cuando no exista una estrategia
   compatible.
4. Delegar en la estrategia concreta la validación.

No agregues `if (tipo.equals(...))` ni un `switch` de tipos en este servicio.
Puedes usar `ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje)` para
representar valores no soportados o reglas incumplidas, o definir excepciones
de dominio con un manejador HTTP específico.

### 4. Integra el servicio con el controlador

Modifica:

```text
src/main/java/com/example/demo/controllers/CategoriaController.java
```

Inyecta `CategoriaService` mediante constructor. Llama al servicio para validar
el `CategoriaRequest` antes de:

- Guardar una categoría en `POST /categoria`.
- Aplicar cambios y guardar en `PUT /categoria/{id}`.

No valides la categoría antes de comprobar que el recurso de un PUT existe:
un ID inexistente debe seguir respondiendo `404`. No cambies el método que
asocia productos y categorías; esa operación no crea ni actualiza la
configuración del tipo de categoría.

Conserva las anotaciones OpenAPI y seguridad del controlador. GET continúa
público; las operaciones de escritura continúan limitadas a `ADMIN`.

### 5. Agrega pruebas unitarias para las estrategias

Crea pruebas en:

```text
src/test/java/com/example/demo/service/categoria/
```

Prueba al menos:

- `GENERAL` se acepta sin icono ni imagen navbar.
- `ICONO` se acepta con icono y se rechaza sin icono.
- `NAVBAR` se acepta con imagen navbar y se rechaza sin ella.
- La selección acepta tipos con espacios externos y distintas mayúsculas.
- Un tipo desconocido y `tipo == null` se rechazan.
- Los errores incluyen un mensaje útil para la persona cliente.

### 6. Prueba la integración HTTP

Extiende `ApiSecurityAndDtoTests` o crea una prueba MVC separada. Comprueba:

- POST con cada tipo válido crea la categoría y devuelve `201`.
- POST con un tipo desconocido o configuración incompleta devuelve `400`.
- PUT válido devuelve `200`; PUT a un ID inexistente devuelve `404`.
- GET de colección y GET por ID no necesitan JWT.
- POST y PUT sin token responden `401`.
- POST y PUT con rol `USER` responden `403`.
- Las solicitudes válidas con rol `ADMIN` pueden crear/actualizar.

Mockea el repositorio en estas pruebas para no depender de PostgreSQL.

### 7. Documenta el contrato en OpenAPI

Actualiza el `@Operation` de POST/PUT en
`CategoriaController` para explicar los valores permitidos de `tipo` y qué
campos requiere cada uno. No marques como protegido un método GET público.

## Ejemplos para probar la API

Ejemplo válido para `ICONO`:

```json
{
  "nombre": "Bebidas",
  "descripcion": "Bebidas frías y calientes",
  "tipo": "ICONO",
  "icono": "fa-mug-hot",
  "imagenNavbar": null
}
```

Ejemplo válido para `NAVBAR`:

```json
{
  "nombre": "Temporada",
  "descripcion": "Productos destacados de temporada",
  "tipo": "NAVBAR",
  "icono": null,
  "imagenNavbar": "https://ejemplo.test/temporada.png"
}
```

Ejemplo inválido para `ICONO` (espera `400 Bad Request`):

```json
{
  "nombre": "Bebidas",
  "descripcion": "Bebidas frías y calientes",
  "tipo": "ICONO",
  "icono": "",
  "imagenNavbar": null
}
```

Para probar escrituras autenticadas, inicia sesión con una cuenta `ADMIN` y
usa **Authorize** en Swagger UI o manda `Authorization: Bearer <token>` con
`curl`. El registro público crea exclusivamente cuentas `USER`.

## Criterios de aceptación

- [ ] Cada tipo tiene una implementación de `CategoriaStrategy`.
- [ ] Spring descubre las estrategias como beans y las inyecta en el servicio.
- [ ] `CategoriaController` no contiene reglas condicionales por tipo.
- [ ] `CategoriaService` selecciona una estrategia; no contiene un `switch`
  con reglas de negocio por tipo.
- [ ] El tipo desconocido y las configuraciones incompletas responden `400`.
- [ ] POST, PUT, GET, `404`, `Location` y seguridad conservan su contrato.
- [ ] Las pruebas cubren reglas individuales e integración HTTP.
- [ ] `./mvnw test` pasa.
- [ ] Agregar un tipo nuevo requiere crear una estrategia y pruebas, sin editar
  las estrategias existentes.

## Preguntas para entregar

1. ¿Qué problema de mantenimiento evita Strategy en esta funcionalidad?
2. ¿Qué responsabilidades tiene `CategoriaController` después del cambio?
3. ¿Por qué las estrategias son componentes Spring y no se crean con `new` en
   el controlador?
4. ¿Qué ventaja ofrece inyectar una lista de estrategias en lugar de
   seleccionar reglas con `switch`?
5. ¿Cuándo sería excesivo aplicar Strategy a esta API?
6. ¿Qué patrón adicional podrías introducir para convertir DTOs a entidades y
   cuándo justificaría hacerlo?

## Referencias

- [Catálogo de patrones de diseño — Refactoring.Guru](https://refactoring.guru/design-patterns/catalog)
- [Strategy — Refactoring.Guru](https://refactoring.guru/design-patterns/strategy)

---

# Solución de referencia

Los siguientes bloques muestran una implementación completa para el proyecto
`SpringAndDesignPatterns`. Cada bloque indica la ruta relativa a `demo/`.
Puedes resolver primero el ejercicio y usar esta sección para comparar.

La selección del patrón sigue Strategy: cada regla vive en un objeto concreto
que comparte el mismo contrato. Spring inyecta la familia de implementaciones
en el servicio; al agregar un tipo, se agrega una estrategia sin editar el
controlador ni las estrategias existentes.

## 1. Contrato común

**Crear:** `src/main/java/com/example/demo/service/categoria/CategoriaStrategy.java`

```java
package com.example.demo.service.categoria;

import java.util.Locale;

import com.example.demo.dto.CategoriaRequest;

public interface CategoriaStrategy {

    boolean supports(String tipo);

    void validate(CategoriaRequest request);

    static String normalizeTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
    }
}
```

## 2. Estrategia para categorías generales

**Crear:** `src/main/java/com/example/demo/service/categoria/CategoriaGeneralStrategy.java`

```java
package com.example.demo.service.categoria;

import org.springframework.stereotype.Component;

import com.example.demo.dto.CategoriaRequest;

@Component
public class CategoriaGeneralStrategy implements CategoriaStrategy {

    @Override
    public boolean supports(String tipo) {
        return "GENERAL".equals(CategoriaStrategy.normalizeTipo(tipo));
    }

    @Override
    public void validate(CategoriaRequest request) {
        // GENERAL no requiere campos adicionales.
    }
}
```

## 3. Estrategia para categorías con icono

**Crear:** `src/main/java/com/example/demo/service/categoria/CategoriaIconoStrategy.java`

```java
package com.example.demo.service.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;

@Component
public class CategoriaIconoStrategy implements CategoriaStrategy {

    @Override
    public boolean supports(String tipo) {
        return "ICONO".equals(CategoriaStrategy.normalizeTipo(tipo));
    }

    @Override
    public void validate(CategoriaRequest request) {
        if (!StringUtils.hasText(request.icono())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo ICONO requiere un campo icono no vacío.");
        }
    }
}
```

## 4. Estrategia para categorías de navegación

**Crear:** `src/main/java/com/example/demo/service/categoria/CategoriaNavbarStrategy.java`

```java
package com.example.demo.service.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;

@Component
public class CategoriaNavbarStrategy implements CategoriaStrategy {

    @Override
    public boolean supports(String tipo) {
        return "NAVBAR".equals(CategoriaStrategy.normalizeTipo(tipo));
    }

    @Override
    public void validate(CategoriaRequest request) {
        if (!StringUtils.hasText(request.imagenNavbar())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo NAVBAR requiere un campo imagenNavbar no vacío.");
        }
    }
}
```

## 5. Servicio que selecciona la estrategia

**Crear:** `src/main/java/com/example/demo/service/CategoriaService.java`

```java
package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.service.categoria.CategoriaStrategy;

@Service
public class CategoriaService {

    private final List<CategoriaStrategy> strategies;

    public CategoriaService(List<CategoriaStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public void validate(CategoriaRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cuerpo de la categoría es obligatorio.");
        }

        String normalizedTipo = CategoriaStrategy.normalizeTipo(request.tipo());
        CategoriaStrategy strategy = strategies.stream()
                .filter(candidate -> candidate.supports(normalizedTipo))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tipo de categoría no válido. Valores permitidos: GENERAL, ICONO y NAVBAR."));

        strategy.validate(request);
    }
}
```

El servicio no conoce reglas para `GENERAL`, `ICONO` ni `NAVBAR`. Solo busca
una estrategia compatible y delega la validación. Las estrategias son
componentes sin estado; si en el futuro alguna necesita consultar datos,
Spring también puede inyectarle un repositorio o servicio.

## 6. Controlador de categorías completo

**Reemplazar completo:** `src/main/java/com/example/demo/controllers/CategoriaController.java`

```java
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
```

En PUT, primero se busca el ID y después se validan los datos. Así se conserva
el `404` para categorías inexistentes incluso si el contenido enviado también
es inválido. Todos los GET mantienen `@PreAuthorize("permitAll()")`; el resto
de las operaciones conserva el rol `ADMIN`.

## 7. Pruebas unitarias de estrategias y selector

**Crear:** `src/test/java/com/example/demo/service/CategoriaServiceTests.java`

```java
package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.service.categoria.CategoriaGeneralStrategy;
import com.example.demo.service.categoria.CategoriaIconoStrategy;
import com.example.demo.service.categoria.CategoriaNavbarStrategy;
import com.example.demo.service.categoria.CategoriaStrategy;

class CategoriaServiceTests {

    private final CategoriaService service = new CategoriaService(List.of(
            new CategoriaGeneralStrategy(),
            new CategoriaIconoStrategy(),
            new CategoriaNavbarStrategy()));

    @Test
    void acceptsGeneralWithoutSpecificFields() {
        assertDoesNotThrow(() -> service.validate(request("GENERAL", null, null)));
    }

    @Test
    void acceptsNormalizedType() {
        assertDoesNotThrow(() -> service.validate(request("  gEnErAl  ", null, null)));
    }

    @Test
    void requiresIconForIconType() {
        assertDoesNotThrow(() -> service.validate(request("ICONO", "fa-star", null)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request("ICONO", " ", null)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "El tipo ICONO requiere un campo icono no vacío.",
                exception.getReason());
    }

    @Test
    void requiresNavbarImageForNavbarType() {
        assertDoesNotThrow(() -> service.validate(request("NAVBAR", null, "/nav.png")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request("NAVBAR", null, "")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "El tipo NAVBAR requiere un campo imagenNavbar no vacío.",
                exception.getReason());
    }

    @Test
    void rejectsUnknownAndNullTypes() {
        ResponseStatusException unknown = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request("OTRO", null, null)));
        ResponseStatusException missing = assertThrows(
                ResponseStatusException.class,
                () -> service.validate(request(null, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, missing.getStatusCode());
    }

    @Test
    void supportsIsCaseInsensitiveAndNullSafe() {
        CategoriaStrategy strategy = new CategoriaIconoStrategy();

        org.junit.jupiter.api.Assertions.assertTrue(strategy.supports(" icono "));
        org.junit.jupiter.api.Assertions.assertFalse(strategy.supports(null));
    }

    private CategoriaRequest request(String tipo, String icono, String imagenNavbar) {
        return new CategoriaRequest("Categoría", null, tipo, icono, imagenNavbar);
    }
}
```

## 8. Pruebas HTTP de integración

**Crear:** `src/test/java/com/example/demo/controllers/CategoriaStrategyIntegrationTests.java`

```java
package com.example.demo.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.Department;
import com.example.demo.repo.categoriaRepo;
import com.example.demo.repo.productoRepo;
import com.example.demo.service.JwtTokenService;

@SpringBootTest
@AutoConfigureMockMvc
class CategoriaStrategyIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockBean
    private categoriaRepo categoriaRepo;

    @MockBean
    private productoRepo productoRepo;

    @BeforeEach
    void resetRepositories() {
        reset(categoriaRepo, productoRepo);
    }

    @Test
    void createsValidIconCategoryAndReturnsLocation() throws Exception {
        when(categoriaRepo.save(any(Department.class))).thenAnswer(invocation -> {
            Department categoria = invocation.getArgument(0);
            categoria.setId("categoria-1");
            return categoria;
        });

        mockMvc.perform(post("/categoria")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Bebidas",
                                  "descripcion": "Bebidas frías y calientes",
                                  "tipo": " ICONO ",
                                  "icono": "fa-mug-hot",
                                  "imagenNavbar": null
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/categoria/categoria-1"))
                .andExpect(jsonPath("$.id").value("categoria-1"))
                .andExpect(jsonPath("$.tipo").value(" ICONO "));
    }

    @Test
    void rejectsUnknownTypeAndMissingIconBeforeSaving() throws Exception {
        mockMvc.perform(post("/categoria")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Otra","tipo":"OTRO"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/categoria")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Bebidas","tipo":"ICONO","icono":" "}
                                """))
                .andExpect(status().isBadRequest());

        org.mockito.Mockito.verifyNoInteractions(categoriaRepo);
    }

    @Test
    void validatesPutOnlyAfterFindingCategory() throws Exception {
        when(categoriaRepo.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(put("/categoria/missing")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Sin tipo"}
                                """))
                .andExpect(status().isNotFound());

        verify(categoriaRepo).findById("missing");
    }

    @Test
    void updatesExistingCategoryAfterStrategyValidation() throws Exception {
        Department categoria = Department.builder()
                .id("categoria-1")
                .nombre("Nombre anterior")
                .tipo("GENERAL")
                .build();
        when(categoriaRepo.findById("categoria-1")).thenReturn(Optional.of(categoria));
        when(categoriaRepo.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/categoria/categoria-1")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Temporada",
                                  "tipo": "NAVBAR",
                                  "imagenNavbar": "/temporada.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("NAVBAR"))
                .andExpect(jsonPath("$.imagenNavbar").value("/temporada.png"));
    }

    @Test
    void categoryReadsRemainPublicAndWritesRemainRoleProtected() throws Exception {
        when(categoriaRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(post("/categoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"General","tipo":"GENERAL"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/categoria")
                        .header("Authorization", userToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"General","tipo":"GENERAL"}
                                """))
                .andExpect(status().isForbidden());
    }

    private String adminToken() {
        return bearerToken("admin", "ADMIN");
    }

    private String userToken() {
        return bearerToken("usuario", "USER");
    }

    private String bearerToken(String username, String role) {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return "Bearer " + jwtTokenService.createToken(authentication);
    }
}
```

## 9. Ejecutar la solución

Desde `demo/`:

```bash
./mvnw test
```

Inicia la aplicación y prueba Swagger UI:

```bash
./mvnw spring-boot:run
```

Abre <http://localhost:8080/swagger-ui/index.html>. Para probar POST o PUT,
inicia sesión con una cuenta `ADMIN` existente y usa el JWT con el botón
**Authorize**. Todos los GET de categorías continúan públicos.
