package com.tuempresa.sistemacitasgestion.dao;

import com.tuempresa.sistemacitasgestion.config.ConexionBD;
import com.tuempresa.sistemacitasgestion.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String SELECT =
            "SELECT id, nombres, apellidos, telefono, correo, activo FROM cliente";

    public List<Cliente> listar() throws SQLException {
        String sql = SELECT + " ORDER BY id";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Cliente> buscar(String texto) throws SQLException {
        String sql = SELECT
                + " WHERE nombres ILIKE ? OR apellidos ILIKE ? OR correo ILIKE ? ORDER BY id";
        String patron = "%" + texto.trim() + "%";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public void insertar(Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (nombres, apellidos, telefono, correo, activo) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setBoolean(5, c.isActivo());
            ps.executeUpdate();
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nombres = ?, apellidos = ?, telefono = ?, "
                + "correo = ?, activo = ? WHERE id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setBoolean(5, c.isActivo());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // excluirId = 0 al insertar; el id del cliente al editar
    public boolean existeCorreo(String correo, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM cliente WHERE LOWER(correo) = LOWER(?) AND id <> ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean tieneCitas(int clienteId) throws SQLException {
        String sql = "SELECT 1 FROM cita_tecnica WHERE cliente_id = ? LIMIT 1";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getBoolean("activo"));
    }
}