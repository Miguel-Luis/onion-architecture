package com.banco.nucleo.modelo;

/**
 * CAPA 1 - MODELO DE DOMINIO.
 *
 * Error propio del negocio: el dominio define sus reglas irrompibles y
 * cómo se rompen, sin depender de errores técnicos de frameworks externos.
 */
public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
