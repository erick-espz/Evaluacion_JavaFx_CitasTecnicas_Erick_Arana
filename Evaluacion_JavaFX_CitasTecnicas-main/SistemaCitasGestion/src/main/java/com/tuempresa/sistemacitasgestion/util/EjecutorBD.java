package com.tuempresa.sistemacitasgestion.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

public final class EjecutorBD {

    private static final ExecutorService EJECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private int contador;

        @Override
        public Thread newThread(Runnable tarea) {
            Thread hilo = new Thread(tarea, "consulta-bd-" + ++contador);
            hilo.setDaemon(true);
            return hilo;
        }
    });

    private EjecutorBD() {}

    public static <T> void ejecutar(Callable<T> operacion, Consumer<T> alCompletar,
                                    Consumer<Throwable> alFallar) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operacion.call();
            }
        };
        tarea.setOnSucceeded(evento -> alCompletar.accept(tarea.getValue()));
        tarea.setOnFailed(evento -> alFallar.accept(tarea.getException()));
        EJECUTOR.execute(tarea);
    }
}
