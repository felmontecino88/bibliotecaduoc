package com.example.bibliotecaduoc.exception;

public class LibroNoDisponibleException extends RuntimeException {

    public LibroNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public LibroNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
