package com.banco.externa.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * CAPA EXTERNA - INFRAESTRUCTURA.
 *
 * Helper técnico para SQLite. Abre conexiones y crea el esquema si no existe.
 * SQLite es una base de datos embebida: vive en un solo archivo (.db) y no
 * requiere instalar ningún servidor.
 *
 * Esto es 100% detalle técnico del anillo externo. El núcleo no sabe que existe.
 */
public class BaseDatosSQLite {

    private final String url;

    public BaseDatosSQLite(String rutaArchivo) {
        this.url = "jdbc:sqlite:" + rutaArchivo;
        inicializarEsquema();
    }

    public Connection abrirConexion() throws SQLException {
        Connection con = DriverManager.getConnection(url);
        con.createStatement().execute("PRAGMA foreign_keys = ON");
        return con;
    }

    private void inicializarEsquema() {
        String clientes = """
            CREATE TABLE IF NOT EXISTS clientes (
                documento TEXT PRIMARY KEY,
                nombre    TEXT NOT NULL
            )
            """;
        String cuentas = """
            CREATE TABLE IF NOT EXISTS cuentas (
                numero            TEXT PRIMARY KEY,
                documento_titular TEXT NOT NULL,
                saldo             REAL NOT NULL,
                FOREIGN KEY (documento_titular) REFERENCES clientes(documento)
            )
            """;
        String movimientos = """
            CREATE TABLE IF NOT EXISTS movimientos (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_cuenta   TEXT NOT NULL,
                tipo            TEXT NOT NULL,
                monto           REAL NOT NULL,
                saldo_resultante REAL NOT NULL,
                fecha           TEXT NOT NULL,
                FOREIGN KEY (numero_cuenta) REFERENCES cuentas(numero)
            )
            """;
        try (Connection con = DriverManager.getConnection(url);
                Statement st = con.createStatement()) {
            st.execute(clientes);
            st.execute(cuentas);
            st.execute(movimientos);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo inicializar la base de datos SQLite", e);
        }
    }
}
