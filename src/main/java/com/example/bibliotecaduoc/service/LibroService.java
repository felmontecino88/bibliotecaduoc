package com.example.bibliotecaduoc.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.example.bibliotecaduoc.dto.UsuarioResponse;
import com.example.bibliotecaduoc.exception.LibroNoDisponibleException;
import com.example.bibliotecaduoc.exception.ResourceNotFoundException;
import com.example.bibliotecaduoc.exception.ServicioExternoException;
import com.example.bibliotecaduoc.model.Libro;
import com.example.bibliotecaduoc.repository.LibroRepository;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final WebClient usuariosWebClient;

    public LibroService(LibroRepository libroRepository,
            @Qualifier("usuariosWebClient") WebClient usuariosWebClient) {
        this.libroRepository = libroRepository;
        this.usuariosWebClient = usuariosWebClient;
    }

    public List<Libro> getLibros() {
        // return libroRepository.obtenerLibros();
        return libroRepository.findAll();

    }

    public Libro saveLibro(Libro libro) {
        // return libroRepository.guardar(libro);
        return libroRepository.save(libro);
    }

    public Libro getLibroId(int id) {
        // return libroRepository.buscarPorId(id);
        return libroRepository.findById(id).orElse(null);
    }

    public Libro updateLibro(Libro libro) {
        // return libroRepository.actualizar(libro);
        return libroRepository.save(libro);
    }

    public String deleteLibro(int id) {
        // libroRepository.eliminar(id);
        // return "producto eliminado";
        libroRepository.deleteById(id);
        return "Libro eliminado";
    }

    // LA ACCIÓN LA HACE EL SERVICE
    public int totalLibros() {
        // return libroRepository.obtenerLibros().size();
        return (int) libroRepository.count();
    }

    // LA ACCIÓN LA HACE EL MODELO
    public int totalLibrosV2() {
        return libroRepository.totalLibros();
    }

    public List<Libro> obtenerPorEditorial(String editorial){
        return libroRepository.selectPorEditorial(editorial);
    }

    /**
     * Registra el préstamo de un libro a un usuario del microservicio "usuarios".
     */
    public Libro prestarLibro(int libroId, int usuarioId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado para id: " + libroId));

        if (libro.getUsuarioId() != null) {
            throw new LibroNoDisponibleException(
                    "El libro " + libroId + " ya está prestado al usuario " + libro.getUsuarioId());
        }

        consultarUsuario(usuarioId);

        libro.setUsuarioId(usuarioId);
        return libroRepository.save(libro);
    }

    /**
     * Marca un libro prestado como devuelto (usuarioId vuelve a null).
     */
    public Libro devolverLibro(int libroId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado para id: " + libroId));

        if (libro.getUsuarioId() == null) {
            throw new LibroNoDisponibleException("El libro " + libroId + " no está actualmente prestado");
        }

        libro.setUsuarioId(null);
        return libroRepository.save(libro);
    }

    /**
     * Obtiene los datos del usuario que actualmente tiene el libro en préstamo.
     */
    public UsuarioResponse obtenerUsuarioDeLibro(int libroId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado para id: " + libroId));

        if (libro.getUsuarioId() == null) {
            throw new ResourceNotFoundException(
                    "El libro " + libroId + " no tiene usuario asociado (disponible)");
        }

        return consultarUsuario(libro.getUsuarioId());
    }

    /**
     * Llama al microservicio "usuarios" para obtener/validar un usuario, traduciendo
     * los errores de WebClient a excepciones propias de este servicio.
     */
    private UsuarioResponse consultarUsuario(int usuarioId) {
        try {
            return usuariosWebClient.get()
                    .uri("/api/v1/usuarios/{id}", usuarioId)
                    .retrieve()
                    .bodyToMono(UsuarioResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException("Usuario no encontrado para id: " + usuarioId, ex);
        } catch (WebClientException ex) {
            throw new ServicioExternoException(
                    "No se pudo contactar al servicio de usuarios", ex);
        }
    }
}
