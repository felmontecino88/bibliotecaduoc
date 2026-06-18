package com.example.bibliotecaduoc.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registrar el préstamo de un libro (PATCH .../prestamo)
 */
public record PrestarLibroRequest(
    @NotNull(message = "usuarioId no puede ser vacío") Integer usuarioId) {
}
