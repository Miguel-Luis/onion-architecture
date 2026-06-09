package com.banco.externa.ui;

import com.banco.nucleo.aplicacion.ServicioCuentas;
import com.banco.nucleo.aplicacion.dto.MovimientoDTO;
import com.banco.nucleo.aplicacion.dto.ResumenCuenta;
import com.banco.nucleo.contratos.IRepositorioCliente;
import com.banco.nucleo.contratos.IRepositorioCuenta;
import com.banco.nucleo.modelo.PoliticaInteres;
import com.banco.nucleo.modelo.SaldoInsuficienteException;
import com.banco.externa.persistencia.BaseDatosSQLite;
import com.banco.externa.persistencia.RepositorioClienteEnMemoria;
import com.banco.externa.persistencia.RepositorioClienteSQLite;
import com.banco.externa.persistencia.RepositorioCuentaEnMemoria;
import com.banco.externa.persistencia.RepositorioCuentaSQLite;

import java.time.format.DateTimeFormatter;

/**
 * CAPA EXTERNA - INTERFAZ GRÁFICA + COMPOSICIÓN.
 *
 * Es el "Composition Root": el único lugar que conoce las clases concretas de
 * infraestructura y las INYECTA hacia adentro. Aquí se arma toda la cebolla.
 *
 * Para MOSTRAR datos, la UI usa SOLO los DTOs que devuelve la capa de
 * Aplicación (ResumenCuenta, MovimientoDTO). Ya NO manipula entidades del
 * dominio como Cuenta o Movimiento: el dominio queda protegido.
 *
 * Las dos únicas referencias al modelo que quedan son legítimas del anillo
 * externo: PoliticaInteres (cableado del Composition Root) y la excepción de
 * negocio que capturamos para la demo.
 *
 * Dirección de las dependencias (siempre HACIA EL NÚCLEO):
 *   Capa Externa (UI / Infra)  ->  Núcleo (Aplicación -> Contratos -> Modelo)
 * El núcleo nunca conoce a la capa externa.
 */
public class Main {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        // Elegir la persistencia SIN tocar el núcleo. Por defecto SQLite;
        // pasa "memoria" como argumento para usar el repositorio en memoria.
        boolean usarMemoria = false;

        // 1) CAPA EXTERNA: implementaciones concretas que firman los contratos.
        //    Aquí está la magia de la cebolla: cambiar de tecnología de
        //    almacenamiento es cambiar SOLO estas dos líneas.
        IRepositorioCuenta repoCuenta;
        IRepositorioCliente repoCliente;
        if (usarMemoria) {
            repoCuenta = new RepositorioCuentaEnMemoria();
            repoCliente = new RepositorioClienteEnMemoria();
        } else {
            BaseDatosSQLite bd = new BaseDatosSQLite("banco.db");
            repoCuenta = new RepositorioCuentaSQLite(bd);
            repoCliente = new RepositorioClienteSQLite(bd);
        }

        // 2) NÚCLEO - política de dominio (2% mensual).
        PoliticaInteres politicaInteres = new PoliticaInteres(0.02);

        // 3) NÚCLEO - servicio de aplicación: recibe las dependencias por constructor.
        ServicioCuentas banco = new ServicioCuentas(repoCuenta, repoCliente, politicaInteres);

        titulo("ARQUITECTURA DE CEBOLLA - DEMO BANCO");
        System.out.println("Persistencia activa: " + (usarMemoria ? "EN MEMORIA" : "SQLite (banco.db)") + "\n");

        // --- Caso de uso: abrir cuentas ---
        banco.abrirCuenta("CC-001", "Ana Pérez", "0001");
        banco.abrirCuenta("CC-002", "Luis Gómez", "0002");
        banco.abrirCuenta("CC-003", "Juan Pérez", "0003");
        System.out.println("Se abrieron 2 cuentas: 0001 (Ana) y 0002 (Luis)\n");

        // --- Caso de uso: depósitos ---
        banco.depositar("0001", 1000);
        banco.depositar("0002", 500);
        banco.depositar("0003", 1000);
        System.out.println("Ana depositó 1000 | Luis depositó 500\n");

        // --- Caso de uso: transferencia ---
        banco.transferir("0001", "0002", 300);
        banco.transferir("0002", "0003", 200);
        System.out.println("Ana transfirió 300 a Luis\n");

        // --- Caso de uso: aplicar intereses (política de dominio) ---
        double interes = banco.aplicarInteresMensual("0002");
        System.out.printf("Se aplicó interés mensual a la cuenta de Luis: +%.2f%n%n", interes);

        // --- Regla de negocio en acción: saldo insuficiente ---
        System.out.println("Intentando que Ana retire 5000 (no tiene saldo)...");
        try {
            banco.retirar("0001", 5000);
        } catch (SaldoInsuficienteException e) {
            System.out.println("  -> El NÚCLEO bloqueó la operación: " + e.getMessage() + "\n");
        }

        // --- Mostrar estado final ---
        imprimirEstado(banco.consultar("0001"));
        imprimirEstado(banco.consultar("0002"));
        imprimirEstado(banco.consultar("0003"));
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
