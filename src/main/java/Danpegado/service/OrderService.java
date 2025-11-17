package Danpegado.service;

import Danpegado.dao.MenuItemDAO;
import Danpegado.dao.OrderDAO;
import Danpegado.model.OrderItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Business logic and transaction around placing an order.
 */
public class OrderService {

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * Place an order (transactional). items -> menuItemId -> quantity Returns
     * the created orderId.
     */
    public int placeOrder(int customerId, Map<Integer, Integer> items, String paymentMethod) throws SQLException {
        // build OrderItem list with current menu prices
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : items.entrySet()) {
            int menuId = e.getKey();
            int qty = e.getValue();
            var menu = menuItemDAO.findById(menuId);
            if (menu == null) {
                throw new SQLException("Menu item not found: " + menuId);
            }
            if (!menu.isAvailable()) {
                throw new SQLException("Menu item not available: " + menu.getName());
            }
            OrderItem oi = new OrderItem(menuId, qty, menu.getPrice());
            orderItems.add(oi);
        }
        return orderDAO.insertOrderWithItems(customerId, paymentMethod, orderItems);
    }

    public void markOrderReady(int orderId) throws SQLException {
        orderDAO.updateOrderStatus(orderId, "ready");
    }

    public void updateOrderStatus(int orderId, String status) throws SQLException {
        orderDAO.updateOrderStatus(orderId, status);
    }
}
