package com.tuempresa.sistemacitasgestion.util;

import com.tuempresa.sistemacitasgestion.model.CitaTecnica;

public final class ValidadorCita {

    private ValidadorCita() {}

    // Devuelve el mensaje de error, o null si la cita es válida
    public static String validar(CitaTecnica c) {
        if (c.getClienteId() <= 0) {
            return "Seleccione un cliente.";
        }
        if (c.getFecha() == null) {
            return "La fecha es obligatoria.";
        }
        if (c.getHora() == null) {
            return "La hora es obligatoria.";
        }
        if (Validador.vacio(c.getTipoServicio()) || c.getTipoServicio().trim().length() > 80) {
            return "El tipo de servicio es obligatorio (máximo 80 caracteres).";
        }
        if (Validador.vacio(c.getDescripcion()) || c.getDescripcion().trim().length() > 255) {
            return "La descripción es obligatoria (máximo 255 caracteres).";
        }
        if (!Validador.estadoValido(c.getEstado())) {
            return "Seleccione un estado válido.";
        }
        return null;
    }
}