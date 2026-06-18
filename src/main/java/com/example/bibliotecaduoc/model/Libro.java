package com.example.bibliotecaduoc.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA para representar un Libro en la base de datos Hibernate creará automáticamente la
 * tabla "libros" si no existe
 */
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "editorial", nullable = false, length = 100)
    private String editorial;

    @Column(name = "fecha_publicacion", nullable = false)
    private int fechaPublicacion;

    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    // id del usuario (microservicio "usuarios") que tiene este libro en préstamo.
    // null = libro disponible. Es una referencia "blanda": no hay FK real entre bases de datos.
    @Column(name = "usuario_id")
    private Integer usuarioId;

    // Constructor sin argumentos (requerido por JPA/Hibernate)
    public Libro() {}

    // Constructor completo
    public Libro(int id, String isbn, String titulo, String editorial, int fechaPublicacion,
            String autor) {
        this.id = id;
        this.isbn = isbn;
        this.titulo = titulo;
        this.editorial = editorial;
        this.fechaPublicacion = fechaPublicacion;
        this.autor = autor;
    }

    // Getters y Setters (manuales porque Lombok no está funcionando)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public int getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(int fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

}
