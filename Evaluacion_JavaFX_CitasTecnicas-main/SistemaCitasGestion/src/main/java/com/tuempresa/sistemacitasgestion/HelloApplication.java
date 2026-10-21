package com.tuempresa.sistemacitasgestion;

import com.tuempresa.sistemacitasgestion.config.ConexionBD;
import com.tuempresa.sistemacitasgestion.util.Alertas;
import com.tuempresa.sistemacitasgestion.util.EjecutorBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("view/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 640);
        scene.getStylesheets().add(HelloApplication.class.getResource("css/estilos.css").toExternalForm());
        stage.setTitle("HexTech - Gestión de citas");
        stage.setScene(scene);
        stage.show();

        EjecutorBD.ejecutar(ConexionBD::probar, conectado -> {
            if (!conectado) {
                Alertas.error("No se pudo conectar con PostgreSQL. Revise db.properties y que el contenedor esté activo.");
            }
        }, error -> Alertas.error("Error inesperado al comprobar la base de datos: " + error.getMessage()));
    }
}