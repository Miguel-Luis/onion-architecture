package com.banco.externa.persistencia;

import com.banco.nucleo.contratos.IRepositorioCliente;
import com.banco.nucleo.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * CAPA EXTERNA - INFRAESTRUCTURA.
 *
 * Implementación del contrato IRepositorioCliente usando SQLite.
 * Es la "otra firma" del mismo contrato que ya firmaba RepositorioClienteEnMemoria.
 * Por eso se puede INTERCAMBIAR una por otra sin tocar el núcleo.
 */
public class RepositorioClienteSQLite implements IRepositorioCliente {

    private final BaseDatosSQLite bd;

    public RepositorioClienteSQLite(BaseDatosSQLite bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Cliente cliente) {
        String sql = """
            INSERT INTO clientes (documento, nombre) VALUES (?, ?)
            ON CONFLICT(documento) DO UPDATE SET nombre = excluded.nombre
            """;
        try (Connection con = bd.abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getDocumento());
            ps.setString(2, cliente.getNombre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        String sql = "SELECT documento, nombre FROM clientes WHERE documento = ?";
        try (Connection con = bd.abrirConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Cliente(
                            rs.getString("documento"),
                            rs.getString("nombre")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }
}
