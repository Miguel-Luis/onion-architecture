package com.banco.nucleo.contratos;

import com.banco.nucleo.modelo.Cliente;

import java.util.Optional;

/**
 * CAPA 2 - SERVICIOS DE DOMINIO (Los Contratos).
 *
 * El núcleo define el CONTRATO de persistencia, pero NO su implementación.
 * Es un "contrato vacío" que la Capa Externa estará obligada a firmar más
 * adelante. El núcleo dicta QUÉ necesita para guardar datos, no CÓMO se hace.
 */
public interface IRepositorioCliente {
    void guardar(Cliente cliente);
    Optional<Cliente> buscarPorDocumento(String documento);
}
