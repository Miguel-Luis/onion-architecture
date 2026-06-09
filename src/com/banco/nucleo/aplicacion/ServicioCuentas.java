package com.banco.nucleo.aplicacion;

import com.banco.nucleo.aplicacion.dto.MovimientoDTO;
import com.banco.nucleo.aplicacion.dto.ResumenCuenta;
import com.banco.nucleo.contratos.IRepositorioCliente;
import com.banco.nucleo.contratos.IRepositorioCuenta;
import com.banco.nucleo.modelo.Cliente;
import com.banco.nucleo.modelo.Cuenta;
import com.banco.nucleo.modelo.PoliticaInteres;
import com.banco.nucleo.modelo.SaldoInsuficienteException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * CAPA 3 - SERVICIOS DE APLICACIÓN (La Frontera del Núcleo).
 *
 * Contiene los CASOS DE USO de la aplicación. Su trabajo es ORQUESTAR:
 * coordina el modelo de dominio con los contratos de persistencia.
 *
 * Es ORQUESTACIÓN AGNÓSTICA: no le importa si la petición viene de una página
 * web, una API REST o una app móvil. Expone la funcionalidad del negocio al
 * mundo exterior y depende solo de los CONTRATOS (interfaces), nunca de
 * implementaciones concretas.
 */
public class ServicioCuentas {

    private final IRepositorioCuenta repositorioCuenta;
    private final IRepositorioCliente repositorioCliente;
    private final PoliticaInteres politicaInteres;

    public ServicioCuentas(IRepositorioCuenta repositorioCuenta,
                            IRepositorioCliente repositorioCliente,
                            PoliticaInteres politicaInteres) {
        this.repositorioCuenta = repositorioCuenta;
        this.repositorioCliente = repositorioCliente;
        this.politicaInteres = politicaInteres;
    }

    public void abrirCuenta(String documentoCliente, String nombreCliente, String numeroCuenta) {
        Cliente cliente = repositorioCliente.buscarPorDocumento(documentoCliente)
                .orElseGet(() -> {
                    Cliente nuevo = new Cliente(documentoCliente, nombreCliente);
                    repositorioCliente.guardar(nuevo);
                    return nuevo;
                });

        Cuenta cuenta = new Cuenta(numeroCuenta, cliente);
        repositorioCuenta.guardar(cuenta);
    }

    public void depositar(String numeroCuenta, double monto) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        cuenta.depositar(monto);
        repositorioCuenta.guardar(cuenta);
    }

    public void retirar(String numeroCuenta, double monto) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        try {
            cuenta.retirar(monto);
        } catch (SaldoInsuficienteException e) {
            throw new OperacionRechazadaException(e.getMessage());
        }
        repositorioCuenta.guardar(cuenta);
    }

    /**
     * Transferencia: caso de uso que coordina dos cuentas.
     * Las reglas (saldo suficiente, montos válidos) siguen viviendo en la entidad;
     * aquí solo traducimos el error de dominio al lenguaje de la aplicación.
     */
    public void transferir(String numeroOrigen, String numeroDestino, double monto) {
        Cuenta origen = obtenerCuenta(numeroOrigen);
        Cuenta destino = obtenerCuenta(numeroDestino);

        try {
            origen.retirar(monto);
        } catch (SaldoInsuficienteException e) {
            throw new OperacionRechazadaException(e.getMessage());
        }
        destino.depositar(monto);

        repositorioCuenta.guardar(origen);
        repositorioCuenta.guardar(destino);
    }

    public double aplicarInteresMensual(String numeroCuenta) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        double interes = politicaInteres.aplicar(cuenta);
        repositorioCuenta.guardar(cuenta);
        return interes;
    }

    /**
     * Caso de uso de consulta: devuelve un DTO listo para mostrar, NO la entidad.
     * La UI nunca toca el dominio: solo recibe datos planos.
     */
    public ResumenCuenta consultar(String numeroCuenta) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        return aResumen(cuenta);
    }

    private Cuenta obtenerCuenta(String numeroCuenta) {
        return repositorioCuenta.buscarPorNumero(numeroCuenta)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe la cuenta: " + numeroCuenta));
    }

    /**
     * Mapeo entidad de dominio -> DTO. Esta traducción es responsabilidad de
     * la capa de Aplicación (la frontera del núcleo).
     */
    private ResumenCuenta aResumen(Cuenta cuenta) {
        List<MovimientoDTO> movimientos = cuenta.getMovimientos().stream()
                .map(m -> new MovimientoDTO(
                        m.getTipo().name(),
                        m.getMonto(),
                        m.getSaldoResultante(),
                        m.getFecha()))
                .toList();

        return new ResumenCuenta(
                cuenta.getNumero(),
                cuenta.getTitular().getNombre(),
                cuenta.getSaldo(),
                movimientos);
    }
}
