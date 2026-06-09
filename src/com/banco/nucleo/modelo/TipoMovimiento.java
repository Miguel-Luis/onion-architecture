package com.banco.nucleo.modelo;

/**
 * CAPA 1 - MODELO DE DOMINIO (El Centro Absoluto).
 *
 * Tipo de movimiento de una cuenta. Estado puro del negocio.
 * Regla de oro: cero dependencias externas (ni SQL, ni ORM, ni red).
 */
public enum TipoMovimiento {
    DEPOSITO,
    RETIRO,
    INTERES
}
