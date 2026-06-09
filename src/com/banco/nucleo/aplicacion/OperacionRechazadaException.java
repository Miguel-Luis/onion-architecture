package com.banco.nucleo.aplicacion;

/**
 * CAPA 3 - SERVICIOS DE APLICACIÓN.
 *
 * Excepción de la capa de Aplicación. Traduce los errores de negocio del
 * dominio (ej. SaldoInsuficienteException) a un lenguaje que la capa externa
 * puede capturar SIN conocer el modelo de dominio.
 *
 * Así la UI depende solo de la frontera (Aplicación) y nunca del núcleo.
 */
public class OperacionRechazadaException extends RuntimeException {
    public OperacionRechazadaException(String mensaje) {
        super(mensaje);
    }
}
