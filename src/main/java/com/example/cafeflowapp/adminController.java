package com.example.cafeflowapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class adminController implements Initializable {

    @FXML private Button btnDashboard;
    @FXML private Button btnMenu;
    @FXML private Button btnOrders;
    @FXML private Button btnLogout;

    @FXML private HBox dashboardView;
    @FXML private VBox ordersView;
    @FXML private VBox menuView;

    @FXML private Button btnTodaySales;
    @FXML private Button btnTotalOrders;
    @FXML private Button btnPendingOrders;
    @FXML private Button btnLowStock;
    @FXML private LineChart<Number, Number> salesChart;

    @FXML private TableView<OrderModel> ordersTable;
    @FXML private TableColumn<OrderModel, String> colOrderId;
    @FXML private TableColumn<OrderModel, String> colCustomer;
    @FXML private TableColumn<OrderModel, String> colOrderItems;
    @FXML private TableColumn<OrderModel, Double> colTotal;
    @FXML private TableColumn<OrderModel, String> colStatus;
    @FXML private Button btnConfirmOrder;
    @FXML private Button btnGenerateBill;

    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> colItemName;
    @FXML private TableColumn<MenuItem, Double> colItemPrice;
    @FXML private TableColumn<MenuItem, Integer> colItemStock;
    @FXML private TextField txtItemName;
    @FXML private TextField txtItemPrice;
    @FXML private TextField txtItemStock;
    @FXML private Button btnAddItem;
    @FXML private Button btnIncreaseStock;
    @FXML private Button btnDeleteItem;

    private ObservableList<MenuItem> menuList;
    private ObservableList<OrderModel> orderList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupNavigation();
        setupDashboardData();
        setupOrdersTable();
        setupMenuTable();
    }

    private void setupNavigation() {
        btnDashboard.setOnAction(e -> switchView(dashboardView));
        btnOrders.setOnAction(e -> switchView(ordersView));
        btnMenu.setOnAction(e -> switchView(menuView));
        btnLogout.setOnAction(e -> System.out.println("Logout Clicked"));
    }

    private void switchView(Node view) {
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);
        ordersView.setVisible(false);
        ordersView.setManaged(false);
        menuView.setVisible(false);
        menuView.setManaged(false);

        view.setVisible(true);
        view.setManaged(true);
    }

    private void setupDashboardData() {
        btnTodaySales.setText("Today's Sales\n$1,500.00");
        btnTotalOrders.setText("Total Orders\n45");
        btnPendingOrders.setText("Pending\n5");
        btnLowStock.setText("Low Stock\n3");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        series.getData().add(new XYChart.Data<>(8, 200));
        series.getData().add(new XYChart.Data<>(10, 450));
        series.getData().add(new XYChart.Data<>(12, 800));
        series.getData().add(new XYChart.Data<>(14, 600));
        salesChart.getData().add(series);
    }

    private void setupOrdersTable() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colOrderItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        orderList = FXCollections.observableArrayList(
                new OrderModel("101", "Alice", "Latte, Cake", 15.0, "Pending"),
                new OrderModel("102", "Bob", "Espresso", 5.0, "Completed"),
                new OrderModel("103", "Charlie", "Tea, Cookie", 8.5, "Pending")
        );
        ordersTable.setItems(orderList);

        btnConfirmOrder.setOnAction(e -> {
            OrderModel selected = ordersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setStatus("Confirmed");
                ordersTable.refresh();
            }
        });

        btnGenerateBill.setOnAction(e -> {
            OrderModel selected = ordersTable.getSelectionModel().getSelectedItem();
            if(selected != null) System.out.println("Bill generated for " + selected.getCustomer());
        });
    }

    private void setupMenuTable() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colItemStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        menuList = FXCollections.observableArrayList(
                new MenuItem("Espresso", 3.50, 50),
                new MenuItem("Cappuccino", 4.50, 40),
                new MenuItem("Blueberry Muffin", 3.00, 15)
        );
        menuTable.setItems(menuList);

        btnAddItem.setOnAction(e -> addNewItem());
        btnDeleteItem.setOnAction(e -> deleteItem());
        btnIncreaseStock.setOnAction(e -> increaseStock());
    }

    private void addNewItem() {
        try {
            String name = txtItemName.getText();
            double price = Double.parseDouble(txtItemPrice.getText());
            int stock = Integer.parseInt(txtItemStock.getText());

            if (!name.isEmpty()) {
                menuList.add(new MenuItem(name, price, stock));
                txtItemName.clear();
                txtItemPrice.clear();
                txtItemStock.clear();
            }
        } catch (NumberFormatException ex) {
            System.out.println("Invalid Input");
        }
    }

    private void deleteItem() {
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            menuList.remove(selected);
        }
    }

    private void increaseStock() {
        MenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStock(selected.getStock() + 10);
            menuTable.refresh();
        }
    }

    public static class OrderModel {
        private String orderId;
        private String customer;
        private String items;
        private Double total;
        private String status;

        public OrderModel(String orderId, String customer, String items, Double total, String status) {
            this.orderId = orderId;
            this.customer = customer;
            this.items = items;
            this.total = total;
            this.status = status;
        }

        public String getOrderId() { return orderId; }
        public String getCustomer() { return customer; }
        public String getItems() { return items; }
        public Double getTotal() { return total; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class MenuItem {
        private String name;
        private Double price;
        private Integer stock;

        public MenuItem(String name, Double price, Integer stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getName() { return name; }
        public Double getPrice() { return price; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}