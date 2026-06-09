package com.banco.nucleo.contratos;

import com.banco.nucleo.modelo.Cuenta;

import java.util.List;
import java.util.Optional;

/**
 * CAPA 2 - SERVICIOS DE DOMINIO (Los Contratos).
 *
 * Interfaz de persistencia para cuentas. La implementación real vive en la
 * Capa Externa (infraestructura). Así se logra la INVERSIÓN DE DEPENDENCIAS:
 * la infraestructura depende del núcleo, nunca al revés.
 */
public interface IRepositorioCuenta {
    void guardar(Cuenta cuenta);
    Optional<Cuenta> buscarPorNumero(String numero);
    List<Cuenta> listarTodas();
}
