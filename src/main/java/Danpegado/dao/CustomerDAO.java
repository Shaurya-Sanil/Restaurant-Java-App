package Danpegado.dao;

import Danpegado.model.Customer;
import Danpegado.util.DBConnection;

import java.sql.*;

public class CustomerDAO {

    public int create(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.executeUpdate();
            try (ResultSet g = ps.getGeneratedKeys()) {
                if (g.next()) {
                    return g.getInt(1);
                }
            }
        }
        return -1;
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT id, name, phone, email FROM customers WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getString("email"));
                }
            }
        }
        return null;
    }
}
