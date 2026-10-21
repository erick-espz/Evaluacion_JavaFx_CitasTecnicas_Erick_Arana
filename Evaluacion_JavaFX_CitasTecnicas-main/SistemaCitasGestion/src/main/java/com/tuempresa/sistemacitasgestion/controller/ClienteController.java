package com.tuempresa.sistemacitasgestion.controller;

import com.tuempresa.sistemacitasgestion.dao.ClienteDAO;
import com.tuempresa.sistemacitasgestion.model.Cliente;
import com.tuempresa.sistemacitasgestion.util.Alertas;
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

import java.sql.SQLException;

public class ClienteController {

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
        String texto = txtBuscar.getText();
        if (Validador.vacio(texto)) {
            cargar();
            return;
        }
        try {
            datos.setAll(dao.buscar(texto));
        } catch (SQLException e) {
            Alertas.errorSql(e);
        }
    }

    @FXML
    private void verTodos() {
        txtBuscar.clear();
        cargar();
    }

    @FXML
    private void guardar() {
        Cliente cliente = leerFormulario(0);
        if (cliente == null) {
            return;
        }
        try {
            dao.insertar(cliente);
            Alertas.exito("Cliente registrado correctamente.");
            refrescar();
        } catch (SQLException e) {
            Alertas.errorSql(e);
        }
    }

    @FXML
    private void actualizar() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Seleccione un cliente de la tabla.");
            return;
        }
        Cliente cliente = leerFormulario(seleccionado.getId());
        if (cliente == null) {
            return;
        }
        try {
            dao.actualizar(cliente);
            Alertas.exito("Cliente actualizado correctamente.");
            refrescar();
        } catch (SQLException e) {
            Alertas.errorSql(e);
        }
    }

    @FXML
    private void eliminar() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alertas.advertencia("Seleccione un cliente de la tabla.");
            return;
        }
        try {
            if (dao.tieneCitas(seleccionado.getId())) {
                Alertas.advertencia("No se puede eliminar a " + seleccionado.getNombreCompleto()
                        + " porque tiene citas asociadas. Elimine primero sus citas.");
                return;
            }
            if (!Alertas.confirmar("¿Eliminar al cliente " + seleccionado.getNombreCompleto() + "?")) {
                return;
            }
            dao.eliminar(seleccionado.getId());
            Alertas.exito("Cliente eliminado correctamente.");
            refrescar();
        } catch (SQLException e) {
            Alertas.errorSql(e);
        }
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
        try {
            datos.setAll(dao.listar());
        } catch (SQLException e) {
            Alertas.errorSql(e);
        }
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
        try {
            if (dao.existeCorreo(correo, id)) {
                Alertas.advertencia("Ya existe un cliente con ese correo.");
                return null;
            }
        } catch (SQLException e) {
            Alertas.errorSql(e);
            return null;
        }
        return new Cliente(id, nombres.trim(), apellidos.trim(), telefono.trim(),
                correo.trim(), chkActivo.isSelected());
    }
}