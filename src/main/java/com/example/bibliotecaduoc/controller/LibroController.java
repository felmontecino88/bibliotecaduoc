package com.example.bibliotecaduoc.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.bibliotecaduoc.dto.CreateLibroRequest;
import com.example.bibliotecaduoc.dto.PokemonResponse;
import com.example.bibliotecaduoc.dto.PrestarLibroRequest;
import com.example.bibliotecaduoc.dto.UpdateLibroRequest;
import com.example.bibliotecaduoc.dto.UsuarioResponse;
import com.example.bibliotecaduoc.exception.ResourceNotFoundException;
import com.example.bibliotecaduoc.mapper.LibroMapper;
import com.example.bibliotecaduoc.model.Libro;
import com.example.bibliotecaduoc.service.LibroService;
import jakarta.validation.Valid;


/**
 * Controller REST modernizado para Java 21 LTS y Spring Boot 3.3+ 100% REST compliant
 */
@RestController
@RequestMapping("/api/v1/libros")
public class LibroController {

        private final LibroService libroService;
        private final WebClient pokeApiWebClient;

        // Constructor injection (mejor práctica 2026)
        public LibroController(LibroService libroService,
                        @Qualifier("pokeApiWebClient") WebClient pokeApiWebClient) {
                this.libroService = libroService;
                this.pokeApiWebClient = pokeApiWebClient;
        }

        @GetMapping
        public ResponseEntity<List<Libro>> listarLibros() {
                List<Libro> libros = libroService.getLibros();
                return ResponseEntity.ok(libros);
        }

        @PostMapping
        public ResponseEntity<Libro> agregarLibro(@Valid @RequestBody CreateLibroRequest request) {
                // @Valid ejecuta validaciones Jakarta automáticamente
                // Si falla → GlobalExceptionHandler.handleValidationErrors() retorna 400

                Libro nuevoLibro = libroService.saveLibro(LibroMapper.toModel(request));
                return ResponseEntity.status(HttpStatus.CREATED).body(nuevoLibro);
        }

        @GetMapping("{id}")
        public ResponseEntity<Libro> buscarLibro(@PathVariable int id) {
                Libro libro = libroService.getLibroId(id);

                if (libro == null) {

                        throw new ResourceNotFoundException("Libro no encontrado para id: " + id);
                }

                return ResponseEntity.ok(libro);
        }

        @PutMapping("{id}")
        public ResponseEntity<Libro> actualizarLibro(@PathVariable int id,
                        @Valid @RequestBody UpdateLibroRequest request) {
                // El ID viene del path, no del body → evita ambigüedad
                Libro libroActualizado = libroService.updateLibro(LibroMapper.toModel(id, request));
                return ResponseEntity.ok(libroActualizado);
        }

        @DeleteMapping("{id}")
        public ResponseEntity<Void> eliminarLibro(@PathVariable int id) {
                libroService.deleteLibro(id);
                return ResponseEntity.noContent().build(); // 204 No Content (estándar REST)
        }

        @GetMapping("/total")
        public ResponseEntity<Integer> totalLibros() {
                int total = libroService.totalLibrosV2();
                return ResponseEntity.ok(total);
        }

        @GetMapping("/editorial/{editorial}")
        public List<Libro> getporEditorial(@PathVariable String editorial) {
                return libroService.obtenerPorEditorial(editorial);
        }

        @GetMapping("/editorial")
        public List<Libro> getporEditorial2(@RequestParam String editorial) {
                return libroService.obtenerPorEditorial(editorial);
        }

        /**
         * Endpoint demostrativo de WebClient consumiendo PokeAPI GET
         * /api/v1/libros/pokeapi?nombre=pikachu
         */
        @GetMapping("/pokeapi")
        public ResponseEntity<PokemonResponse> consultarPokemon(
                        @RequestParam(name = "nombre") String nombre) {

                PokemonResponse pokemon = pokeApiWebClient.get()
                                .uri("/pokemon-species/{nombre}", nombre) // Endpoint más simple
                                .retrieve().bodyToMono(PokemonResponse.class).block();

                return ResponseEntity.ok(pokemon);
        }

        /**
         * Registra el préstamo de un libro a un usuario (microservicio "usuarios").
         * 409 si el libro ya está prestado, 404 si el libro o el usuario no existen.
         */
        @PatchMapping("{id}/prestamo")
        public ResponseEntity<Libro> prestarLibro(@PathVariable int id,
                        @Valid @RequestBody PrestarLibroRequest request) {
                Libro libro = libroService.prestarLibro(id, request.usuarioId());
                return ResponseEntity.ok(libro);
        }

        /**
         * Marca un libro prestado como devuelto. 409 si el libro no estaba prestado.
         */
        @DeleteMapping("{id}/prestamo")
        public ResponseEntity<Libro> devolverLibro(@PathVariable int id) {
                Libro libro = libroService.devolverLibro(id);
                return ResponseEntity.ok(libro);
        }

        /**
         * Obtiene los datos del usuario que actualmente tiene el libro en préstamo.
         */
        @GetMapping("{id}/usuario")
        public ResponseEntity<UsuarioResponse> obtenerUsuarioDeLibro(@PathVariable int id) {
                UsuarioResponse usuario = libroService.obtenerUsuarioDeLibro(id);
                return ResponseEntity.ok(usuario);
        }

}
