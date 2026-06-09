package com.banco.externa.ui;

import com.banco.nucleo.aplicacion.OperacionRechazadaException;
import com.banco.nucleo.aplicacion.ServicioCuentas;
import com.banco.nucleo.aplicacion.dto.MovimientoDTO;
import com.banco.nucleo.aplicacion.dto.ResumenCuenta;
import com.banco.externa.config.Bootstrap;

import java.time.format.DateTimeFormatter;

/**
 * CAPA EXTERNA - INTERFAZ GRÁFICA.
 *
 * La UI es PURA presentación. Solo habla con la capa de Aplicación:
 *  - llama a sus casos de uso (depositar, transferir, ...),
 *  - muestra los DTOs que recibe (ResumenCuenta, MovimientoDTO),
 *  - captura excepciones de Aplicación (OperacionRechazadaException).
 *
 * NO tiene NINGUNA referencia al modelo de dominio: no conoce Cuenta,
 * Movimiento, PoliticaInteres ni las excepciones del núcleo. El ensamblado de
 * las piezas concretas vive en el Composition Root (externa.config.Bootstrap).
 *
 * Dirección de las dependencias (siempre HACIA EL NÚCLEO):
 *   Capa Externa (UI)  ->  Núcleo (Aplicación)
 * El núcleo nunca conoce a la capa externa.
 */
public class Main {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        // Elegir la persistencia SIN tocar el núcleo. Por defecto SQLite;
        // pasa "memoria" como argumento para usar el repositorio en memoria.
        boolean usarMemoria = args.length > 0 && args[0].equalsIgnoreCase("memoria");

        // El Composition Root arma todo y nos entrega el servicio de aplicación.
        ServicioCuentas banco = Bootstrap.crearBanco(usarMemoria);

        titulo("ARQUITECTURA DE CEBOLLA - DEMO BANCO");
        System.out.println("Persistencia activa: " + (usarMemoria ? "EN MEMORIA" : "SQLite (banco.db)") + "\n");

        // --- Caso de uso: abrir cuentas ---
        banco.abrirCuenta("CC-001", "Ana Pérez", "0001");
        banco.abrirCuenta("CC-002", "Luis Gómez", "0002");
        //banco.abrirCuenta("CC-003", "Juan Pérez", "0003");
        System.out.println("Se abrieron 2 cuentas: 0001 (Ana) y 0002 (Luis)\n");

        // --- Caso de uso: depósitos ---
        banco.depositar("0001", 1000);
        banco.depositar("0002", 500);
        //banco.depositar("0003", 1000);
        System.out.println("Ana depositó 1000 | Luis depositó 500\n");

        // --- Caso de uso: transferencia ---
        banco.transferir("0001", "0002", 300);
        //banco.transferir("0002", "0003", 200);
        System.out.println("Ana transfirió 300 a Luis\n");

        // --- Caso de uso: aplicar intereses ---
        double interes = banco.aplicarInteresMensual("0002");
        System.out.printf("Se aplicó interés mensual a la cuenta de Luis: +%.2f%n%n", interes);

        // --- Regla de negocio en acción: saldo insuficiente ---
        System.out.println("Intentando que Ana retire 5000 (no tiene saldo)...");
        try {
            banco.retirar("0001", 5000);
        } catch (OperacionRechazadaException e) {
            System.out.println("  -> Operación rechazada por la aplicación: " + e.getMessage() + "\n");
        }

        // --- Mostrar estado final ---
        imprimirEstado(banco.consultar("0001"));
        imprimirEstado(banco.consultar("0002"));
        //imprimirEstado(banco.consultar("0003"));
    }

    private static void imprimirEstado(ResumenCuenta cuenta) {
        titulo("CUENTA " + cuenta.numero() + " - " + cuenta.titular());
        System.out.printf("Saldo actual: %.2f%n", cuenta.saldo());
        System.out.println("Movimientos:");
        for (MovimientoDTO m : cuenta.movimientos()) {
            System.out.printf("  [%s] %-9s %10.2f  (saldo: %.2f)%n",
                    m.fecha().format(FMT),
                    m.tipo(),
                    m.monto(),
                    m.saldoResultante());
        }
        System.out.println();
    }

    private static void titulo(String texto) {
        System.out.println("==================================================");
        System.out.println(" " + texto);
        System.out.println("==================================================");
    }
}
