package Danpegado.dao;

import Danpegado.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationDAO {

    public boolean isTableAvailable(int tableId, LocalDateTime time) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM reservations WHERE table_id = ? AND reservation_time = ? AND status = 'booked'";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            ps.setString(2, time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") == 0;
                }
            }
        }
        return false;
    }

    public int createReservation(int customerId, int tableId, LocalDateTime time, int partySize) throws SQLException {
        String sql = "INSERT INTO reservations (customer_id, table_id, reservation_time, party_size, status) VALUES (?, ?, ?, ?, 'booked')";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setInt(2, tableId);
            ps.setString(3, time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.setInt(4, partySize);
            ps.executeUpdate();
            try (ResultSet g = ps.getGeneratedKeys()) {
                if (g.next()) {
                    return g.getInt(1);
                }
            }
        }
        return -1;
    }
}
