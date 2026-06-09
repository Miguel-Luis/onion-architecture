package com.banco.nucleo.aplicacion.dto;

import java.time.LocalDateTime;

/**
 * CAPA 3 - DTO (Data Transfer Object) de salida.
 *
 * Objeto plano y de solo lectura que la capa de Aplicación entrega al mundo
 * exterior. La UI muestra ESTO, no la entidad de dominio Movimiento.
 * Así, si la entidad cambia por dentro, la UI no se rompe.
 */
public record MovimientoDTO(
        String tipo,
        double monto,
        double saldoResultante,
        LocalDateTime fecha) {
}
