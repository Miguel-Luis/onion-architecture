package com.banco.nucleo.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CAPA 1 - MODELO DE DOMINIO (El Centro Absoluto).
 *
 * Cuenta: raíz del agregado. Aquí viven las REGLAS DE NEGOCIO IRROMPIBLES,
 * el "estado y comportamiento del negocio puro".
 *
 * Regla de oro de esta capa: cero dependencias externas. Esta clase no sabe
 * qué es un ORM, no conoce SQL, ni sabe que existe la red. Las reglas se
 * cumplen siempre, vengan los datos de donde vengan.
 */
public class Cuenta {
    private final String numero;
    private final Cliente titular;
    private double saldo;
    private final List<Movimiento> movimientos;

    public Cuenta(String numero, Cliente titular) {
        this.numero = numero;
        this.titular = titular;
        this.saldo = 0.0;
        this.movimientos = new ArrayList<>();
    }

    /**
     * Reconstrucción (rehidratación) desde almacenamiento.
     *
     * Lo usan los repositorios para volver a armar una cuenta que YA existía
     * (con su saldo y movimientos históricos), sin re-aplicar reglas ni cambiar
     * las fechas originales. No es para crear cuentas nuevas: para eso está el
     * constructor y los métodos de negocio (depositar, retirar...).
     */
    public static Cuenta reconstituir(String numero, Cliente titular,
                                    double saldo, List<Movimiento> movimientos) {
        Cuenta cuenta = new Cuenta(numero, titular);
        cuenta.saldo = saldo;
        cuenta.movimientos.addAll(movimientos);
        return cuenta;
    }

    public void depositar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a 0");
        }
        saldo += monto;
        movimientos.add(new Movimiento(TipoMovimiento.DEPOSITO, monto, saldo));
    }

    public void retirar(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a 0");
        }
        if (monto > saldo) {
            throw new SaldoInsuficienteException(
                "Saldo insuficiente. Saldo actual: " + saldo + ", intento de retiro: " + monto);
        }
        saldo -= monto;
        movimientos.add(new Movimiento(TipoMovimiento.RETIRO, monto, saldo));
    }

    /**
     * Acredita intereses. La tasa la decide la PoliticaInteres (también dominio).
     */
    public void acreditarIntereses(double monto) {
        if (monto < 0) {
            throw new IllegalArgumentException("El interés no puede ser negativo");
        }
        saldo += monto;
        movimientos.add(new Movimiento(TipoMovimiento.INTERES, monto, saldo));
    }

    public String getNumero() {
        return numero;
    }

    public Cliente getTitular() {
        return titular;
    }

    public double getSaldo() {
        return saldo;
    }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}
