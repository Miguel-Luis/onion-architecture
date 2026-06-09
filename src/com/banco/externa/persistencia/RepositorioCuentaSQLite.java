package com.banco.externa.persistencia;

import com.banco.nucleo.contratos.IRepositorioCuenta;
import com.banco.nucleo.modelo.Cliente;
import com.banco.nucleo.modelo.Cuenta;
import com.banco.nucleo.modelo.Movimiento;
import com.banco.nucleo.modelo.TipoMovimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CAPA EXTERNA - INFRAESTRUCTURA.
 *
 * Implementación del contrato IRepositorioCuenta usando SQLite.
 * Traduce entre la entidad de dominio (Cuenta) y las filas de las tablas.
 * El núcleo no sabe nada de SQL: eso vive aquí, en el anillo externo.
 */
public class RepositorioCuentaSQLite implements IRepositorioCuenta {

    private final BaseDatosSQLite bd;

    public RepositorioCuentaSQLite(BaseDatosSQLite bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Cuenta cuenta) {
        String upsertCuenta = """
            INSERT INTO cuentas (numero, documento_titular, saldo) VALUES (?, ?, ?)
            ON CONFLICT(numero) DO UPDATE SET saldo = excluded.saldo
            """;
        try (Connection con = bd.abrirConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(upsertCuenta)) {
                ps.setString(1, cuenta.getNumero());
                ps.setString(2, cuenta.getTitular().getDocumento());
                ps.setDouble(3, cuenta.getSaldo());
                ps.executeUpdate();
            }

            // Estrategia simple: borrar y reinsertar los movimientos de la cuenta.
            try (PreparedStatement del = con.prepareStatement(
                    "DELETE FROM movimientos WHERE numero_cuenta = ?")) {
                del.setString(1, cuenta.getNumero());
                del.executeUpdate();
            }

            String insMov = """
                INSERT INTO movimientos (numero_cuenta, tipo, monto, saldo_resultante, fecha)
                VALUES (?, ?, ?, ?, ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(insMov)) {
                for (Movimiento m : cuenta.getMovimientos()) {
                    ps.setString(1, cuenta.getNumero());
                    ps.setString(2, m.getTipo().name());
                    ps.setDouble(3, m.getMonto());
                    ps.setDouble(4, m.getSaldoResultante());
                    ps.setString(5, m.getFecha().toString());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cuenta", e);
        }
    }

    @Override
    public Optional<Cuenta> buscarPorNumero(String numero) {
        String sql = """
            SELECT c.numero, c.saldo, cl.documento, cl.nombre
            FROM cuentas c
            JOIN clientes cl ON cl.documento = c.documento_titular
            WHERE c.numero = ?
            """;
        try (Connection con = bd.abrirConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Cliente titular = new Cliente(rs.getString("documento"), rs.getString("nombre"));
                double saldo = rs.getDouble("saldo");
                List<Movimiento> movimientos = cargarMovimientos(con, numero);
                return Optional.of(Cuenta.reconstituir(numero, titular, saldo, movimientos));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cuenta", e);
        }
    }

    @Override
    public List<Cuenta> listarTodas() {
        List<Cuenta> resultado = new ArrayList<>();
        String sql = """
            SELECT c.numero, c.saldo, cl.documento, cl.nombre
            FROM cuentas c
            JOIN clientes cl ON cl.documento = c.documento_titular
            """;
        try (Connection con = bd.abrirConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String numero = rs.getString("numero");
                Cliente titular = new Cliente(rs.getString("documento"), rs.getString("nombre"));
                double saldo = rs.getDouble("saldo");
                List<Movimiento> movimientos = cargarMovimientos(con, numero);
                resultado.add(Cuenta.reconstituir(numero, titular, saldo, movimientos));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar cuentas", e);
        }
        return resultado;
    }

    private List<Movimiento> cargarMovimientos(Connection con, String numeroCuenta) throws SQLException {
        List<Movimiento> movimientos = new ArrayList<>();
        String sql = """
            SELECT tipo, monto, saldo_resultante, fecha
            FROM movimientos
            WHERE numero_cuenta = ?
            ORDER BY id
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new Movimiento(
                            TipoMovimiento.valueOf(rs.getString("tipo")),
                            rs.getDouble("monto"),
                            rs.getDouble("saldo_resultante"),
                            LocalDateTime.parse(rs.getString("fecha"))));
                }
            }
        }
        return movimientos;
    }
}
