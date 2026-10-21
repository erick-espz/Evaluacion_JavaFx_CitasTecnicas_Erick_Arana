package com.tuempresa.sistemacitasgestion.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class Validador {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+\\- ]{8,20}$");
    private static final Set<String> ESTADOS =
            Set.of("Pendiente", "En proceso", "Finalizada", "Cancelada");

    private Validador() {}

    public static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    public static boolean nombreValido(String texto) {
        if (vacio(texto)) {
            return false;
        }
        return texto.trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+");
    }

    public static boolean correoValido(String correo) {
        return correo != null && CORREO.matcher(correo.trim()).matches();
    }

    public static boolean telefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono.trim()).matches();
    }

    public static boolean estadoValido(String estado) {
        return estado != null && ESTADOS.contains(estado);
    }
}