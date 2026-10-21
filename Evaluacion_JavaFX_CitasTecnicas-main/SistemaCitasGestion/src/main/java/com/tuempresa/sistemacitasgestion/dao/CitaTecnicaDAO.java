package com.tuempresa.sistemacitasgestion.dao;

import com.tuempresa.sistemacitasgestion.config.ConexionBD;
import com.tuempresa.sistemacitasgestion.model.CitaTecnica;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CitaTecnicaDAO {

    private static final String SELECT =
            "SELECT ct.id, ct.cliente_id, c.nombres || ' ' || c.apellidos AS cliente_nombre, "
                    + "ct.fecha, ct.hora, ct.tipo_servicio, ct.descripcion, ct.estado "
                    + "FROM cita_tecnica ct JOIN cliente c ON c.id = ct.cliente_id";

    private static final String ORDEN = " ORDER BY ct.fecha, ct.hora, ct.id";

    public List<CitaTecnica> listar() throws SQLException {
        return filtrar(null, null);
    }

    // estado o fecha en null significa sin filtro
    public List<CitaTecnica> filtrar(String estado, LocalDate fecha) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (estado != null) {
            sql.append(" AND ct.estado = ?");
            params.add(estado);
        }
        if (fecha != null) {
            sql.append(" AND ct.fecha = ?");
            params.add(Date.valueOf(fecha));
        }
        sql.append(ORDEN);

        List<CitaTecnica> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void insertar(CitaTecnica c) throws SQLException {
        String sql = "INSERT INTO cita_tecnica (cliente_id, fecha, hora, tipo_servicio, descripcion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, c);
            ps.executeUpdate();
        }
    }

    public void actualizar(CitaTecnica c) throws SQLException {
        String sql = "UPDATE cita_tecnica SET cliente_id = ?, fecha = ?, hora = ?, "
                + "tipo_servicio = ?, descripcion = ?, estado = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            asignar(ps, c);
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM cita_tecnica WHERE id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existeCliente(int clienteId) throws SQLException {
        String sql = "SELECT 1 FROM cliente WHERE id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void asignar(PreparedStatement ps, CitaTecnica c) throws SQLException {
        ps.setInt(1, c.getClienteId());
        ps.setDate(2, Date.valueOf(c.getFecha()));
        ps.setTime(3, Time.valueOf(c.getHora()));
        ps.setString(4, c.getTipoServicio().trim());
        ps.setString(5, c.getDescripcion().trim());
        ps.setString(6, c.getEstado());
    }

    private CitaTecnica mapear(ResultSet rs) throws SQLException {
        return new CitaTecnica(
                rs.getInt("id"),
                rs.getInt("cliente_id"),
                rs.getString("cliente_nombre"),
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora").toLocalTime(),
                rs.getString("tipo_servicio"),
                rs.getString("descripcion"),
                rs.getString("estado"));
    }
}