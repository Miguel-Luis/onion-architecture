package com.banco.externa.persistencia;

import com.banco.nucleo.contratos.IRepositorioCliente;
import com.banco.nucleo.modelo.Cliente;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * CAPA EXTERNA - INFRAESTRUCTURA.
 *
 * Implementación concreta del contrato IRepositorioCliente (en memoria).
 */
public class RepositorioClienteEnMemoria implements IRepositorioCliente {

    private final Map<String, Cliente> datos = new HashMap<>();

    @Override
    public void guardar(Cliente cliente) {
        datos.put(cliente.getDocumento(), cliente);
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return Optional.ofNullable(datos.get(documento));
    }
}
