package com.banco.nucleo.modelo;

/**
 * CAPA 1 - MODELO DE DOMINIO.
 *
 * Entidad pura que modela el problema (como Restaurante o Repartidor en el
 * ejemplo de clase). No sabe nada de bases de datos ni de la red.
 */
public class Cliente {
    private final String documento;
    private final String nombre;

    public Cliente(String documento, String nombre) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        this.documento = documento;
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }
}
