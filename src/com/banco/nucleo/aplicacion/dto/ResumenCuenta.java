package com.banco.nucleo.aplicacion.dto;

import java.util.List;

/**
 * CAPA 3 - DTO (Data Transfer Object) de salida.
 *
 * Vista "lista para mostrar" de una cuenta. La capa de Aplicación arma este
 * objeto a partir de la entidad de dominio y se lo entrega a la UI.
 * La UI depende de este DTO, NO de la entidad Cuenta.
 */
public record ResumenCuenta(
        String numero,
        String titular,
        double saldo,
        List<MovimientoDTO> movimientos) {
}
