Repo para el curso DSY1103 Fullstack I


# Guia 1: Conexión a PostgreSQL (NeonTech) con Hibernate/JPA

Esta guía documenta el proceso completo para conectar la aplicación BibliotecaDUOC a una base de datos PostgreSQL en NeonTech usando Hibernate/JPA.

---

## 📋 Tabla de Contenidos

1. [Prerequisitos](#prerequisitos)
2. [Configuración de NeonTech](#configuración-de-neontech)
3. [Configuración del Proyecto](#configuración-del-proyecto)
4. [Migración del Repository](#migración-del-repository)
5. [Verificación](#verificación)
6. [Troubleshooting](#troubleshooting)

---

## 1. Prerequisitos

### Dependencias Maven

Ya agregadas en `pom.xml`:

```xml
<!-- Spring Data JPA + Hibernate -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Software Necesario

- ✅ Java 21+
- ✅ Maven 3.9+
- ✅ Cuenta en NeonTech (https://neon.tech)

---

## 2. Configuración de NeonTech

### Paso 1: Crear Proyecto en Neon

1. Ingresa a https://console.neon.tech
2. Click en **"New Project"**
3. Configura:
   - **Project name**: `bibliotecaduoc`
   - **Region**: Selecciona el más cercano (ej: `US East`)
   - **PostgreSQL version**: `16` (recomendado)
4. Click en **"Create Project"**

### Paso 2: Obtener Credenciales

Neon te mostrará una pantalla con la cadena de conexión:

![asd](doc/Captura%20de%20pantalla%202026-04-13%20104434.png)

![asd](doc/Captura%20de%20pantalla%202026-04-13%20104531.png)


```
postgresql://username:password@ep-cool-silence-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
```

**Descompón la URL**:
- **Endpoint**: `ep-cool-silence-123456.us-east-2.aws.neon.tech`
- **Database**: `neondb` (puedes crear una nueva base de datos si prefieres, pero este es el nombre por defecto)
- **Username**: `username` (nombre de usuario)
- **Password**: Copia el password (¡guárdalo!, no se muestra de nuevo)


## 3. Configuración del Proyecto

### Paso 1: Configurar `application.properties`

Edita `src/main/resources/application.properties`:


```properties
# ===================================
# PostgreSQL + Hibernate (JPA) - NeonTech
# ===================================

spring.datasource.url=jdbc:postgresql://ep-cool-silence-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
spring.datasource.username=tu_username_real
spring.datasource.password=tu_password_real
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración de Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### Explicación de `ddl-auto`

| Valor | Comportamiento |
|-------|----------------|
| `update` | Actualiza schema sin borrar datos |
| `create` | Borra y recrea tabla en cada inicio (pierde datos) |
| `create-drop` | Borra tabla al cerrar aplicación |
| `validate` | Solo valida que schema coincida (producción) |
| `none` | No hace nada automáticamente |

### Paso 2: Verificar Entidad `Libro`

La clase `Libro` ya está configurada como entidad JPA:

```java
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    // ... otros campos
}
```

**Anotaciones clave**:
- `@Entity`: Marca la clase como entidad JPA
- `@Table(name = "libros")`: Nombre de la tabla en la BD
- `@Id`: Clave primaria
- `@GeneratedValue`: PostgreSQL genera el ID automáticamente
- `@Column`: Configuración de cada columna

---

## 4. Cambios al Repository

**Elimina** la implementación manual y crea una **interface**:

```java
package com.example.bibliotecaduoc.repository;

import com.example.bibliotecaduoc.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Integer> {

    // Métodos automáticos heredados de JpaRepository:
    // - findAll()
    // - findById(int id)
    // - save(Libro libro)
    // - deleteById(int id)
    // - count()

    // Métodos custom (Spring Data JPA los implementa automáticamente)
    Optional<Libro> findByIsbn(String isbn);

    List<Libro> findByAutorContainingIgnoreCase(String autor);
}
```


## 5. Cambios al Service
Necesitas actualizar `LibroService` para usar los métodos JPA:

```java
// Antes (con ArrayList)
libroRepository.obtenerLibros();

// Después (con JPA)
libroRepository.findAll();
```

## 6. Verificación

### Paso 1: Compilar

```bash
./mvnw clean compile
```

Debe completar sin errores.

### Paso 2: Ejecutar Aplicación

```bash
./mvnw spring-boot:run
```

**Busca en los logs**:

```
Hibernate: create table if not exists libros (
    id int4 generated by default as identity,
    isbn varchar(20) not null,
    titulo varchar(200) not null,
    editorial varchar(100) not null,
    fecha_publicacion int4 not null,
    autor varchar(150) not null,
    primary key (id)
)
```

Si ves esto, **¡la tabla se creó correctamente!** 

### Paso 3: Verificar en Neon SQL Editor

1. Ve a https://console.neon.tech
2. Click en **SQL Editor**
3. Ejecuta:
   ```sql
   SELECT * FROM libros;
   ```

Debería mostrar la tabla vacía pero existente.

### Paso 4: Probar API con Swagger

1. Abre http://localhost:8080/swagger-ui.html
2. Prueba **POST /api/v1/libros**:
   ```json
   {
     "isbn": "978-0-13-468599-1",
     "titulo": "Clean Code",
     "editorial": "Prentice Hall",
     "fechaPublicacion": 2008,
     "autor": "Robert C. Martin"
   }
   ```
3. Verifica con **GET /api/v1/libros**


---

# Guía 2: Comunicación con el microservicio "usuarios"

Este repo ("libros", puerto `8080`) se comunica vía **WebClient** con el microservicio hermano
[`dsy1103_usuarios`](../dsy1103_usuarios) (puerto `8081`) para registrar y consultar qué usuario
tiene actualmente en préstamo cada libro.

## Modelo de datos

`Libro` agrega un campo `usuarioId` (`Integer`, columna `usuario_id`, nullable):

- `usuarioId == null` → el libro está **disponible**.
- `usuarioId == <id>` → el libro está **prestado** al usuario con ese id en el microservicio
  "usuarios". Es una referencia "blanda": no existe una foreign key real entre las dos bases
  de datos (viven en proyectos Neon distintos).

## Configuración

En `application.properties`:

```properties
server.port=8080
usuarios.service.url=http://localhost:8081
```

`usuarios.service.url` se inyecta en el bean `usuariosWebClient` (`config/WebClientConfig.java`),
separado del `pokeApiWebClient` ya existente. Ambos beans tienen nombre explícito
(`@Bean("pokeApiWebClient")` / `@Bean("usuariosWebClient")`) y se inyectan con `@Qualifier`
para evitar ambigüedad.

## Endpoints nuevos

| Método | Ruta                          | Descripción                                                  |
|--------|--------------------------------|---------------------------------------------------------------|
| PATCH  | `/api/v1/libros/{id}/prestamo` | Presta el libro `{id}` al usuario indicado en el body          |
| DELETE | `/api/v1/libros/{id}/prestamo` | Marca el libro `{id}` como devuelto (`usuarioId` → `null`)     |
| GET    | `/api/v1/libros/{id}/usuario`  | Devuelve los datos del usuario que tiene el libro `{id}`        |

### Ejemplos

```bash
# Prestar el libro 1 al usuario 5
curl -X PATCH http://localhost:8080/api/v1/libros/1/prestamo \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 5}'

# Ver quién tiene el libro 1
curl http://localhost:8080/api/v1/libros/1/usuario

# Devolver el libro 1
curl -X DELETE http://localhost:8080/api/v1/libros/1/prestamo
```

### Respuestas de error

| Código | Cuándo ocurre                                                                 |
|--------|---------------------------------------------------------------------------------|
| 404    | El libro `{id}` no existe, o el `usuarioId` no existe en el microservicio "usuarios" |
| 409    | `PATCH .../prestamo` sobre un libro ya prestado, o `DELETE .../prestamo` sobre un libro no prestado |
| 503    | El microservicio "usuarios" no responde (apagado, timeout, etc.)                |

## Levantar ambos servicios

1. Levanta primero `usuarios` (puerto `8081`) — ver su propio `README.md` para configurar su
   conexión a Neon (usa un proyecto Neon **distinto** al de este repo).
2. Levanta `bibliotecaduoc` (puerto `8080`):
   ```bash
   ./mvnw spring-boot:run
   ```
3. Al iniciar, Hibernate agrega automáticamente la columna `usuario_id` a la tabla `libros`
   existente (`spring.jpa.hibernate.ddl-auto=update`).

## Apuntar a un deploy real

Si más adelante despliegas `usuarios` en otra máquina/URL, basta con cambiar
`usuarios.service.url` en `application.properties` (por ejemplo,
`usuarios.service.url=https://usuarios.mi-dominio.com`) — no se requiere ningún otro cambio
de código.

