package com.banco.externa.config;

import com.banco.nucleo.aplicacion.ServicioCuentas;
import com.banco.nucleo.contratos.IRepositorioCliente;
import com.banco.nucleo.contratos.IRepositorioCuenta;
import com.banco.nucleo.modelo.PoliticaInteres;
import com.banco.externa.persistencia.BaseDatosSQLite;
import com.banco.externa.persistencia.RepositorioClienteEnMemoria;
import com.banco.externa.persistencia.RepositorioClienteSQLite;
import com.banco.externa.persistencia.RepositorioCuentaEnMemoria;
import com.banco.externa.persistencia.RepositorioCuentaSQLite;

/**
 * CAPA EXTERNA - COMPOSITION ROOT.
 *
 * Único lugar que conoce las clases CONCRETAS (infraestructura y política de
 * dominio) y las ENSAMBLA para construir el servicio de aplicación.
 *
 * Al separar el cableado de la UI, la interfaz (Main) queda libre: solo habla
 * con la capa de Aplicación y nunca toca el modelo de dominio.
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static ServicioCuentas crearBanco(boolean usarMemoria) {
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

        PoliticaInteres politicaInteres = new PoliticaInteres(0.02);

        return new ServicioCuentas(repoCuenta, repoCliente, politicaInteres);
    }
}
