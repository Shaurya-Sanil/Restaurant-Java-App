package Danpegado.service;

import Danpegado.dao.ReservationDAO;
import Danpegado.dao.CustomerDAO;
import Danpegado.model.Customer;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReservationService {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    public int createCustomerIfNotExists(Customer c) throws SQLException {
        // This simple example always creates customer; in real app you'd check duplicates
        return customerDAO.create(c);
    }

    public int bookReservation(Customer customer, int tableId, LocalDateTime time, int partySize) throws SQLException {
        int customerId = createCustomerIfNotExists(customer);
        boolean avail = reservationDAO.isTableAvailable(tableId, time);
        if (!avail) {
            throw new SQLException("Table not available at that time");
        }
        return reservationDAO.createReservation(customerId, tableId, time, partySize);
    }
}
