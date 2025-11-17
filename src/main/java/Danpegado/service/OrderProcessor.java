package Danpegado.service;

import Danpegado.dao.OrderDAO;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Processes orders asynchronously.
 */
public class OrderProcessor {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OrderDAO orderDAO = new OrderDAO();

    public void processOrderAsync(int orderId) {
        executor.submit(() -> {
            try {
                System.out.println("[Processor] Starting processing for order: " + orderId);
                // Simulate some work
                Thread.sleep(5000);
                orderDAO.updateOrderStatus(orderId, "completed");
                System.out.println("[Processor] Finished processing for order: " + orderId);
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
                try {
                    orderDAO.updateOrderStatus(orderId, "failed");
                } catch (SQLException ex) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
