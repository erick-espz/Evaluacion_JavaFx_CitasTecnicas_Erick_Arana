package com.tuempresa.sistemacitasgestion.controller;


import com.tuempresa.sistemacitasgestion.dao.CitaTecnicaDAO;
import com.tuempresa.sistemacitasgestion.dao.ClienteDAO;
import com.tuempresa.sistemacitasgestion.model.CitaTecnica;
import com.tuempresa.sistemacitasgestion.model.Cliente;
import com.tuempresa.sistemacitasgestion.util.Alertas;
import com.tuempresa.sistemacitasgestion.util.EjecutorBD;
import com.tuempresa.sistemacitasgestion.util.ValidadorCita;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;


import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class CitaController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private VBox panelPrincipal;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private DatePicker dpFiltroFecha;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtServicio;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private TextArea txtDescripcion;
    @FXML private TableView<CitaTecnica> tablaCitas;
    @FXML private TableColumn<CitaTecnica, Integer> colId;
    @FXML private TableColumn<CitaTecnica, String> colCliente;
    @FXML private TableColumn<CitaTecnica, LocalDate> colFecha;
    @FXML private TableColumn<CitaTecnica, LocalTime> colHora;
    @FXML private TableColumn<CitaTecnica, String> colServicio;
    @FXML private TableColumn<CitaTecnica, String> colEstado;


    private final CitaTecnicaDAO dao = new CitaTecnicaDAO();
    private final ClienteDAO clienteDao = new ClienteDAO();
    private final ObservableList<CitaTecnica> datos = FXCollections.observableArrayList();
    private boolean ignorarFiltros;
    private boolean operacionEnCurso;
    private long solicitudListado;
    private long solicitudClientes;


    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colServicio.setCellValueFactory(new PropertyValueFactory<>("tipoServicio"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));


        tablaCitas.setItems(datos);
        tablaCitas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarFormulario(actual);
            }
        });


        mostrarPrompt(cmbCliente);
        mostrarPrompt(cmbFiltroEstado);
        cmbCliente.setOnShowing(e -> cargarClientes());
        cmbFiltroEstado.valueProperty().addListener((obs, anterior, actual) -> aplicarFiltros());
        dpFiltroFecha.valueProperty().addListener((obs, anterior, actual) -> aplicarFiltros());


        cargarClientes();
        aplicarFiltros();
    }


    @FXML
    private void guardar() {
        if (operacionEnCurso) {
            return;
        }
        CitaTecnica cita = leerFormulario(0);
        if (cita == null) {
            return;
        }
        ejecutarGuardado(cita, false);
    }


    @FXML
    private void actualizar() {
        if (operacionEnCurso) {
            return;
        }
        CitaTecnica seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.advertencia("Seleccione una cita de la tabla.");
            return;
        }
        CitaTecnica cita = leerFormulario(seleccionada.getId());
        if (cita == null) {
            return;
        }
        ejecutarGuardado(cita, true);
    }


    @FXML
    private void eliminar() {
        if (operacionEnCurso) {
            return;
        }
        CitaTecnica seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            Alertas.advertencia("Seleccione una cita de la tabla.");
            return;
        }
        if (!Alertas.confirmar("¿Eliminar la cita de " + seleccionada.getClienteNombre()
                + " del " + seleccionada.getFecha() + "?")) {
            return;
        }
        comenzarOperacion();
        EjecutorBD.ejecutar(() -> {
            dao.eliminar(seleccionada.getId());
            return null;
        }, resultado -> {
            finalizarOperacion();
            Alertas.exito("Cita eliminada correctamente.");
            refrescar();
        }, error -> {
            finalizarOperacion();
            mostrarError(error);
        });
    }


    @FXML
    private void limpiar() {
        cmbCliente.setValue(null);
        dpFecha.setValue(null);
        txtHora.clear();
        txtServicio.clear();
        txtDescripcion.clear();
        cmbEstado.setValue("Pendiente");
        tablaCitas.getSelectionModel().clearSelection();
    }


    @FXML
    private void limpiarFiltros() {
        ignorarFiltros = true;
        cmbFiltroEstado.setValue(null);
        dpFiltroFecha.setValue(null);
        ignorarFiltros = false;
        aplicarFiltros();
    }


    // null en un filtro = sin filtro
    private void aplicarFiltros() {
        if (ignorarFiltros) {
            return;
        }
        String estado = cmbFiltroEstado.getValue();
        LocalDate fecha = dpFiltroFecha.getValue();
        long solicitud = ++solicitudListado;
        EjecutorBD.ejecutar(() -> dao.filtrar(estado, fecha),
                citas -> {
                    if (solicitud == solicitudListado) {
                        datos.setAll(citas);
                    }
                }, this::mostrarError);
    }


    private void refrescar() {
        limpiar();
        aplicarFiltros();
    }


    private void cargarClientes() {
        Cliente elegido = cmbCliente.getValue();
        Integer idElegido = elegido == null ? null : elegido.getId();
        cargarClientes(idElegido);
    }

    private void cargarClientes(Integer idElegido) {
        long solicitud = ++solicitudClientes;
        EjecutorBD.ejecutar(clienteDao::listar, clientes -> {
            if (solicitud != solicitudClientes) {
                return;
            }
            cmbCliente.getItems().setAll(clientes);
            if (idElegido != null) {
                elegirCliente(idElegido);
            }
        }, this::mostrarError);
    }


    private boolean elegirCliente(int id) {
        for (Cliente c : cmbCliente.getItems()) {
            if (c.getId() == id) {
                cmbCliente.setValue(c);
                return true;
            }
        }
        return false;
    }


    private void seleccionarCliente(int id) {
        if (!elegirCliente(id)) {
            cargarClientes(id);
        }
    }


    private void cargarFormulario(CitaTecnica c) {
        seleccionarCliente(c.getClienteId());
        dpFecha.setValue(c.getFecha());
        txtHora.setText(c.getHora() == null ? "" : c.getHora().format(HORA));
        txtServicio.setText(c.getTipoServicio());
        txtDescripcion.setText(c.getDescripcion());
        cmbEstado.setValue(c.getEstado());
    }


    private CitaTecnica leerFormulario(int id) {
        Cliente cliente = cmbCliente.getValue();
        if (cliente == null) {
            Alertas.advertencia("Seleccione un cliente.");
            return null;
        }


        LocalTime hora;
        try {
            hora = LocalTime.parse(txtHora.getText().trim(), HORA);
        } catch (DateTimeParseException e) {
            Alertas.advertencia("La hora debe tener el formato HH:mm (por ejemplo, 09:30).");
            return null;
        }


        CitaTecnica cita = new CitaTecnica(id, cliente.getId(), cliente.getNombreCompleto(),
                dpFecha.getValue(), hora, txtServicio.getText().trim(),
                txtDescripcion.getText().trim(), cmbEstado.getValue());


        String error = ValidadorCita.validar(cita);
        if (error != null) {
            Alertas.advertencia(error);
            return null;
        }


        return cita;
    }

    private void ejecutarGuardado(CitaTecnica cita, boolean actualizar) {
        comenzarOperacion();
        EjecutorBD.ejecutar(() -> {
            if (!dao.existeCliente(cita.getClienteId())) {
                return false;
            }
            if (actualizar) {
                dao.actualizar(cita);
            } else {
                dao.insertar(cita);
            }
            return true;
        }, guardado -> {
            finalizarOperacion();
            if (!guardado) {
                Alertas.advertencia("El cliente seleccionado ya no existe.");
                return;
            }
            Alertas.exito(actualizar
                    ? "Cita actualizada correctamente."
                    : "Cita registrada correctamente.");
            refrescar();
        }, error -> {
            finalizarOperacion();
            mostrarError(error);
        });
    }

    private void comenzarOperacion() {
        operacionEnCurso = true;
        panelPrincipal.setDisable(true);
        ++solicitudListado;
        ++solicitudClientes;
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



    private <T> void mostrarPrompt(ComboBox<T> combo) {
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? combo.getPromptText() : item.toString());
            }
        });
    }
}