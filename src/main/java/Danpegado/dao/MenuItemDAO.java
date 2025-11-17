package Danpegado.dao;

import Danpegado.model.MenuItem;
import Danpegado.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public List<MenuItem> getAvailableItems() throws SQLException {
        String sql = "SELECT id, name, description, price, is_available FROM menu_items WHERE is_available = 1";
        List<MenuItem> items = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getBoolean("is_available")
                ));
            }
        }
        return items;
    }

    public MenuItem findById(int id) throws SQLException {
        String sql = "SELECT id, name, description, price, is_available FROM menu_items WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MenuItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getBoolean("is_available")
                    );
                }
            }
        }
        return null;
    }
}
