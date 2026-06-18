package com.example.bibliotecaduoc.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler modernizado con Problem Details API (RFC 7807) Estándar Spring Boot 3.x /
 * 2026 para APIs REST
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public GlobalExceptionHandler() {
        System.out.println("✅ GlobalExceptionHandler SE HA REGISTRADO CORRECTAMENTE");
    }

    /**
     * Maneja errores de validación Jakarta con Problem Details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        System.out
                .println("🔴 GlobalExceptionHandler EJECUTADO - Errores de validación detectados");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Error de validación en los datos enviados");

        problem.setTitle("Validation Error");
        problem.setProperty("timestamp", Instant.now());

        // Extraer errores con streams modernos
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage()
                                : "Valor inválido"));

        problem.setProperty("errors", errors);

        System.out.println("🔴 Errores encontrados: " + errors);
        return problem;
    }

    /**
     * Maneja errores de parseo de JSON (antes de validaciones)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleJsonParseError(HttpMessageNotReadableException ex) {
        System.out.println("🟡 Error de parseo JSON capturado");
        System.out.println("🟡 Mensaje: " + ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Error al procesar el JSON enviado");

        problem.setTitle("JSON Parse Error");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("detalle", ex.getMostSpecificCause().getMessage());
        return problem;
    }

    /**
     * Maneja recursos no encontrados con Problem Details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        problem.setTitle("Resource Not Found");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Maneja conflictos de disponibilidad de un libro (ya prestado / no estaba prestado)
     */
    @ExceptionHandler(LibroNoDisponibleException.class)
    public ProblemDetail handleLibroNoDisponible(LibroNoDisponibleException ex) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        problem.setTitle("Libro No Disponible");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Maneja fallas de comunicación con servicios externos (p.ej. microservicio "usuarios")
     */
    @ExceptionHandler(ServicioExternoException.class)
    public ProblemDetail handleServicioExterno(ServicioExternoException ex) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());

        problem.setTitle("Servicio Externo No Disponible");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    /**
     * Maneja errores generales del servidor
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        System.out.println("🔴 EXCEPCIÓN CAPTURADA: " + ex.getClass().getName());
        System.out.println("🔴 Mensaje: " + ex.getMessage());
        ex.printStackTrace();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor");

        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("detalle", ex.getMessage());
        problem.setProperty("tipoExcepcion", ex.getClass().getSimpleName());
        return problem;
    }
}
