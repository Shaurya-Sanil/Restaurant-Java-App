package Danpegado.dao;

import Danpegado.model.OrderItem;
import Danpegado.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public int insertOrderWithItems(int customerId, String paymentMethod, List<OrderItem> items) throws SQLException {
        String insertOrder = "INSERT INTO orders (customer_id, status, payment_method, total_amount) VALUES (?, 'placed', ?, 0)";
        String insertItem = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateTotal = "UPDATE orders SET total_amount = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS); PreparedStatement psItem = conn.prepareStatement(insertItem); PreparedStatement psUpdate = conn.prepareStatement(updateTotal)) {

                psOrder.setInt(1, customerId);
                psOrder.setString(2, paymentMethod);
                psOrder.executeUpdate();
                try (ResultSet g = psOrder.getGeneratedKeys()) {
                    if (!g.next()) {
                        throw new SQLException("Failed to create order");
                    }
                    int orderId = g.getInt(1);

                    double total = 0.0;
                    for (OrderItem oi : items) {
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, oi.getMenuItemId());
                        psItem.setInt(3, oi.getQuantity());
                        psItem.setDouble(4, oi.getPrice());
                        psItem.executeUpdate();
                        total += oi.getPrice() * oi.getQuantity();
                    }

                    psUpdate.setDouble(1, total);
                    psUpdate.setInt(2, orderId);
                    psUpdate.executeUpdate();

                    conn.commit();
                    return orderId;
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<OrderItem> getItemsForOrder(int orderId) throws SQLException {
        String sql = "SELECT id, order_id, menu_item_id, quantity, price FROM order_items WHERE order_id = ?";
        List<OrderItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getInt("id"));
                    oi.setOrderId(rs.getInt("order_id"));
                    oi.setMenuItemId(rs.getInt("menu_item_id"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setPrice(rs.getDouble("price"));
                    list.add(oi);
                }
            }
        }
        return list;
    }

    public void updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }
}
