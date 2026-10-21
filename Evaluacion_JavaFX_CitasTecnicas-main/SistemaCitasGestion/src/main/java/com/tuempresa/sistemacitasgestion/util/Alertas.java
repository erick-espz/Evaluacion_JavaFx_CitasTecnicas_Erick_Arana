package com.tuempresa.sistemacitasgestion.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.SQLException;

public final class Alertas {

    private static final String CSS =
            "/com/tuempresa/sistemacitasgestion/css/estilos.css";

    private Alertas() {}

    public static void exito(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Operación exitosa", mensaje);
    }

    public static void advertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Atención", mensaje);
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    public static void errorSql(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensaje = switch (estado) {
            case "23505" -> "Ya existe un registro con ese valor único (por ejemplo, el correo).";
            case "23503" -> "La operación viola la relación entre cliente y citas.";
            case "08001", "08006" ->
                    "No se pudo conectar con la base de datos. Verifique que PostgreSQL esté activo.";
            default -> "Error de base de datos: " + e.getMessage();
        };
        error(mensaje);
    }

    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmación");
        alerta.setHeaderText(null);
        estilizar(alerta);
        return alerta.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        estilizar(alerta);
        alerta.showAndWait();
    }

    private static void estilizar(Alert alerta) {
        alerta.getDialogPane().getStylesheets().add(Alertas.class.getResource(CSS).toExternalForm());
        alerta.getDialogPane().getStyleClass().add("dialogo");
    }
}