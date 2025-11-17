package Danpegado.app;

import Danpegado.dao.MenuItemDAO;
import Danpegado.model.MenuItem;
import Danpegado.service.OrderProcessor;
import Danpegado.service.OrderService;
import Danpegado.service.ReservationService;
import Danpegado.model.Customer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class MainApp extends Application {

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderService orderService = new OrderService();
    private final OrderProcessor orderProcessor = new OrderProcessor();
    private final ReservationService reservationService = new ReservationService();

    private final ObservableList<MenuItem> menuItems = FXCollections.observableArrayList();
    private final Map<Integer, Integer> cart = new HashMap<>(); // menuId -> qty
    private final ListView<String> cartView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Restaurant: Reservation & Online Ordering");

        TabPane tabs = new TabPane();
        Tab orderTab = new Tab("Order");
        orderTab.setClosable(false);
        orderTab.setContent(buildOrderPane());

        Tab reserveTab = new Tab("Reservations");
        reserveTab.setClosable(false);
        reserveTab.setContent(buildReservationPane());

        tabs.getTabs().addAll(orderTab, reserveTab);

        loadMenuItems();

        Scene scene = new Scene(tabs, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Node buildOrderPane() {
        TableView<MenuItem> table = new TableView<>();
        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        TableColumn<MenuItem, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", data.getValue().getPrice())));

        table.getColumns().addAll(nameCol, priceCol);
        table.setItems(menuItems);
        table.setPrefWidth(450);

        Spinner<Integer> qtySpinner = new Spinner<>(1, 20, 1);
        Button addBtn = new Button("Add to Cart");
        addBtn.setOnAction(e -> {
            MenuItem selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "Select item", "Please select a menu item first.");
                return;
            }
            cart.put(selected.getId(), cart.getOrDefault(selected.getId(), 0) + qtySpinner.getValue());
            refreshCartView();
        });

        VBox left = new VBox(10, table, new HBox(10, new Label("Qty"), qtySpinner, addBtn));
        left.setPadding(new Insets(10));

        Button placeOrderBtn = new Button("Place Order");
        placeOrderBtn.setOnAction(e -> placeOrder());

        VBox right = new VBox(10, new Label("Cart:"), cartView, placeOrderBtn);
        right.setPadding(new Insets(10));
        right.setPrefWidth(300);

        HBox root = new HBox(10, left, right);
        root.setPadding(new Insets(10));
        return root;
    }

    private Node buildReservationPane() {
        TextField nameFld = new TextField();
        TextField phoneFld = new TextField();
        TextField emailFld = new TextField();
        Spinner<Integer> tableId = new Spinner<>(1, 10, 1);
        Spinner<Integer> partySize = new Spinner<>(1, 10, 2);

        DatePicker datePicker = new DatePicker();
        Spinner<Integer> hour = new Spinner<>(0, 23, 19);
        Spinner<Integer> minute = new Spinner<>(0, 59, 0);

        Button bookBtn = new Button("Book Reservation");
        bookBtn.setOnAction(e -> {
            try {
                String name = nameFld.getText().trim();
                String phone = phoneFld.getText().trim();
                String email = emailFld.getText().trim();
                if (name.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Name required");
                    return;
                }
                if (datePicker.getValue() == null) {
                    showAlert(Alert.AlertType.ERROR, "Validation", "Please pick a date");
                    return;
                }
                LocalDateTime time = datePicker.getValue().atTime(hour.getValue(), minute.getValue());
                Customer c = new Customer(name, phone, email);
                int rid = reservationService.bookReservation(c, tableId.getValue(), time, partySize.getValue());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation created (id=" + rid + ")");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
                ex.printStackTrace();
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(8);
        grid.setHgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameFld, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneFld, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailFld, 1, 2);
        grid.add(new Label("Table ID:"), 0, 3);
        grid.add(tableId, 1, 3);
        grid.add(new Label("Party Size:"), 0, 4);
        grid.add(partySize, 1, 4);
        grid.add(new Label("Date:"), 0, 5);
        grid.add(datePicker, 1, 5);
        grid.add(new Label("Hour:"), 0, 6);
        grid.add(hour, 1, 6);
        grid.add(new Label("Minute:"), 0, 7);
        grid.add(minute, 1, 7);
        grid.add(bookBtn, 1, 8);

        return grid;
    }

    private void loadMenuItems() {
        try {
            List<MenuItem> list = menuItemDAO.getAvailableItems();
            menuItems.setAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }
    }

    private void refreshCartView() {
        List<String> lines = new ArrayList<>();
        double total = 0.0;
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            try {
                MenuItem mi = menuItemDAO.findById(e.getKey());
                if (mi == null) {
                    continue;
                }
                int q = e.getValue();
                double line = mi.getPrice() * q;
                total += line;
                lines.add(mi.getName() + " x" + q + " = " + String.format("%.2f", line));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        lines.add("-----");
        lines.add("Total: " + String.format("%.2f", total));
        cartView.setItems(FXCollections.observableArrayList(lines));
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Cart empty", "Add items first.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("Cash");
        dialog.setTitle("Payment Method");
        dialog.setHeaderText("Enter payment method (e.g., Cash, Card)");
        Optional<String> res = dialog.showAndWait();
        if (!res.isPresent()) {
            return;
        }
        String paymentMethod = res.get();

        // For demo: create a simple customer (in production you'd lookup)
        Customer demoCustomer = new Customer("Guest", "000", "guest@example.com");
        try {
            Map<Integer, Integer> itemsCopy = new HashMap<>(cart);
            int orderId = orderService.placeOrder(demoCustomer == null ? 0 : new Danpegado.dao.CustomerDAO().create(demoCustomer), itemsCopy, paymentMethod);
            showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Order ID: " + orderId);
            System.out.println("[MainApp] Order placed: " + orderId);

            // process asynchronously with OrderProcessor
            orderProcessor.processOrderAsync(orderId);

            cart.clear();
            refreshCartView();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Error placing order", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(t);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }

    @Override
    public void stop() throws Exception {
        orderProcessor.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
