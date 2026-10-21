package com.tuempresa.sistemacitasgestion.controller;

import com.tuempresa.sistemacitasgestion.dao.ClienteDAO;
import com.tuempresa.sistemacitasgestion.model.Cliente;
import com.tuempresa.sistemacitasgestion.util.Alertas;
import com.tuempresa.sistemacitasgestion.util.EjecutorBD;
import com.tuempresa.sistemacitasgestion.util.Validador;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class ClienteController {

    @FXML private VBox panelPrincipal;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colActivo;

    private final ClienteDAO dao = new ClienteDAO();
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();
    private long solicitudListado;
    private boolean operacionEnCurso;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colActivo.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().isActivo() ? "Sí" : "No"));

        tablaClientes.setItems(datos);
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarFormulario(actual);
            }
        });
        cargar();
    }

    @FXML
    private void buscar() {
        String texto = txtBuscar.getText().trim();
        if (Validador.vacio(texto)) {
            cargar();
            return;
        }
        long solicitud = ++solicitudListado;
        EjecutorBD.ejecutar(() -> dao.buscar(texto),
                clientes -> {
                    if (solicitud == solicitudListado) {
                        datos.setAll(clientes);
                    }
                }, this::mostrarError);
    }

    @FXML
    private void verTodos() {
        txtBuscar.clear();
        cargar();
    }

    @FXML
    private void guardar() {
        if (operacionEnCurso) {
            return;
        }
        Cliente cliente = leerFormulario(0);
        if (cliente == null) {
            return;
        }
        ejecutarGuardado(cliente, false);
    }

    @FXML
    private void actualizar() {
        if (operacionEnCurso) {
            return;
        }
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Seleccione un cliente de la tabla.");
            return;
        }
        Cliente cliente = leerFormulario(seleccionado.getId());
        if (cliente == null) {
            return;
        }
        ejecutarGuardado(cliente, true);
    }

    @FXML
    private void eliminar() {
        if (operacionEnCurso) {
            return;
        }
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Seleccione un cliente de la tabla.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar al cliente " + seleccionado.getNombreCompleto() + "?")) {
            return;
        }
        operacionEnCurso = true;
        panelPrincipal.setDisable(true);
        ++solicitudListado;
        EjecutorBD.ejecutar(() -> {
            if (dao.tieneCitas(seleccionado.getId())) {
                return false;
            }
            dao.eliminar(seleccionado.getId());
            return true;
        }, eliminado -> {
            finalizarOperacion();
            if (!eliminado) {
                Alertas.advertencia("No se puede eliminar a " + seleccionado.getNombreCompleto()
                        + " porque tiene citas asociadas. Elimine primero sus citas.");
                return;
            }
            Alertas.exito("Cliente eliminado correctamente.");
            refrescar();
        }, error -> {
            finalizarOperacion();
            mostrarError(error);
        });
    }

    @FXML
    private void limpiar() {
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        chkActivo.setSelected(true);
        tablaClientes.getSelectionModel().clearSelection();
    }

    private void cargar() {
        long solicitud = ++solicitudListado;
        EjecutorBD.ejecutar(dao::listar,
                clientes -> {
                    if (solicitud == solicitudListado) {
                        datos.setAll(clientes);
                    }
                }, this::mostrarError);
    }

    private void refrescar() {
        limpiar();
        txtBuscar.clear();
        cargar();
    }

    private void cargarFormulario(Cliente c) {
        txtNombres.setText(c.getNombres());
        txtApellidos.setText(c.getApellidos());
        txtTelefono.setText(c.getTelefono());
        txtCorreo.setText(c.getCorreo());
        chkActivo.setSelected(c.isActivo());
    }

    private Cliente leerFormulario(int id) {
        String nombres = txtNombres.getText();
        String apellidos = txtApellidos.getText();
        String telefono = txtTelefono.getText();
        String correo = txtCorreo.getText();

        if (Validador.vacio(nombres) || Validador.vacio(apellidos)
                || Validador.vacio(telefono) || Validador.vacio(correo)) {
            Alertas.advertencia("Todos los campos son obligatorios.");
            return null;
        }
        if (!Validador.nombreValido(nombres) || !Validador.nombreValido(apellidos)) {
            Alertas.advertencia("Nombres y apellidos solo deben contener letras.");
            return null;
        }
        if (!Validador.telefonoValido(telefono)) {
            Alertas.advertencia("El teléfono solo admite números, +, - y espacios (8 a 20 caracteres).");
            return null;
        }
        if (!Validador.correoValido(correo)) {
            Alertas.advertencia("El correo no tiene un formato válido.");
            return null;
        }
        return new Cliente(id, nombres.trim(), apellidos.trim(), telefono.trim(),
                correo.trim(), chkActivo.isSelected());
    }

    private void ejecutarGuardado(Cliente cliente, boolean actualizar) {
        operacionEnCurso = true;
        panelPrincipal.setDisable(true);
        ++solicitudListado;
        EjecutorBD.ejecutar(() -> {
            if (dao.existeCorreo(cliente.getCorreo(), cliente.getId())) {
                return false;
            }
            if (actualizar) {
                dao.actualizar(cliente);
            } else {
                dao.insertar(cliente);
            }
            return true;
        }, guardado -> {
            finalizarOperacion();
            if (!guardado) {
                Alertas.advertencia("Ya existe un cliente con ese correo.");
                return;
            }
            Alertas.exito(actualizar
                    ? "Cliente actualizado correctamente."
                    : "Cliente registrado correctamente.");
            refrescar();
        }, error -> {
            finalizarOperacion();
            mostrarError(error);
        });
    }

    private void finalizarOperacion() {
        operacionEnCurso = false;
        panelPrincipal.setDisable(false);
    }

    private void mostrarError(Throwable error) {
        if (error instanceof SQLException sqlException) {
            Alertas.errorSql(sqlException);
        } else {
            Alertas.error("Error inesperado: " + error.getMessage());
        }
    }
}