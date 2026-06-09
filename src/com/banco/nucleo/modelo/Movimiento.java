package com.banco.nucleo.modelo;

import java.time.LocalDateTime;

/**
 * CAPA 1 - MODELO DE DOMINIO.
 *
 * Movimiento inmutable: una operación que ya ocurrió sobre una cuenta.
 * Solo estado y comportamiento del negocio puro.
 */
public class Movimiento {
    private final TipoMovimiento tipo;
    private final double monto;
    private final double saldoResultante;
    private final LocalDateTime fecha;

    public Movimiento(TipoMovimiento tipo, double monto, double saldoResultante) {
        this(tipo, monto, saldoResultante, LocalDateTime.now());
    }

    /**
     * Constructor de RECONSTRUCCIÓN: lo usan los repositorios para rehidratar un
     * movimiento ya guardado, conservando su fecha original.
     */
    public Movimiento(TipoMovimiento tipo, double monto, double saldoResultante, LocalDateTime fecha) {
        this.tipo = tipo;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.fecha = fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public double getSaldoResultante() {
        return saldoResultante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
