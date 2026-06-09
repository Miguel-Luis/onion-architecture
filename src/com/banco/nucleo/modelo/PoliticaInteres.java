package com.banco.nucleo.modelo;

/**
 * CAPA 1 - MODELO DE DOMINIO.
 *
 * Comportamiento de negocio puro que no cabe en una sola entidad: la política
 * del banco para calcular intereses. Sigue siendo núcleo (cero dependencias
 * externas), por eso vive junto al modelo.
 */
public class PoliticaInteres {

    private final double tasaMensual;

    public PoliticaInteres(double tasaMensual) {
        if (tasaMensual < 0) {
            throw new IllegalArgumentException("La tasa no puede ser negativa");
        }
        this.tasaMensual = tasaMensual;
    }

    /**
     * Calcula y acredita el interés sobre el saldo actual de la cuenta.
     * @return el monto de interés acreditado
     */
    public double aplicar(Cuenta cuenta) {
        double interes = cuenta.getSaldo() * tasaMensual;
        cuenta.acreditarIntereses(interes);
        return interes;
    }
}
