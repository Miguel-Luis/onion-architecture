package com.banco.externa.persistencia;

import com.banco.nucleo.contratos.IRepositorioCuenta;
import com.banco.nucleo.modelo.Cuenta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CAPA EXTERNA - INFRAESTRUCTURA.
 *
 * Aquí se "firma" el contrato IRepositorioCuenta definido por el núcleo.
 * Es un detalle técnico que cambia rápido: hoy es un HashMap, mañana podría
 * ser MySQL o Mongo, y el núcleo NO se enteraría.
 */
public class RepositorioCuentaEnMemoria implements IRepositorioCuenta {

    private final Map<String, Cuenta> datos = new HashMap<>();

    @Override
    public void guardar(Cuenta cuenta) {
        datos.put(cuenta.getNumero(), cuenta);
    }

    @Override
    public Optional<Cuenta> buscarPorNumero(String numero) {
        return Optional.ofNullable(datos.get(numero));
    }

    @Override
    public List<Cuenta> listarTodas() {
        return new ArrayList<>(datos.values());
    }
}
