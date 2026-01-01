package com.example.cafeflowapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class adminController implements Initializable {

    @FXML private Button btnDashboard, btnMenu, btnOrders, btnLogout;
    @FXML private HBox dashboardView;
    @FXML private VBox ordersView, menuView;
    @FXML private Button btnTodaySales, btnTotalOrders, btnPendingOrders, btnLowStock;
    @FXML private LineChart<Number, Number> salesChart;
    @FXML private Label lblOverviewItems;
    @FXML private Label lblOverviewCompleted;
    @FXML private Label lblOverviewPending;
    @FXML private TableView<OrderModel> ordersTable;
    @FXML private TableColumn<OrderModel, String> colOrderId, colCustomer, colOrderItems, colStatus;
    @FXML private TableColumn<OrderModel, Double> colTotal;
    @FXML private Button btnConfirmOrder, btnGenerateBill;
    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> colItemName;
    @FXML private TableColumn<MenuItem, Double> colItemPrice;
    @FXML private TableColumn<MenuItem, Integer> colItemStock;
    @FXML private TextField txtItemName, txtItemPrice, txtItemStock;
    @FXML private Button btnAddItem, btnIncreaseStock, btnDeleteItem;

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
        btnDashboard.setOnAction(e -> { switchView(dashboardView); setupDashboardData(); });
        btnOrders.setOnAction(e -> { switchView(ordersView); refreshOrders(); });
        btnMenu.setOnAction(e -> { switchView(menuView); refreshMenu(); });
        btnLogout.setOnAction(e -> logout());
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(root);

            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchView(Node view) {
        dashboardView.setVisible(false); dashboardView.setManaged(false);
        ordersView.setVisible(false); ordersView.setManaged(false);
        menuView.setVisible(false); menuView.setManaged(false);
        view.setVisible(true); view.setManaged(true);
    }

    private void setupDashboardData() {
        // fetching data for Cards
        double todaySales = Database.getTodaySales();
        int totalOrders = Database.getTotalOrdersCount();
        int pendingOrders = Database.getPendingOrdersCount();
        int lowStock = Database.getLowStockCount();

        btnTodaySales.setText("Today's Sales\n$" + String.format("%.2f", todaySales));
        btnTotalOrders.setText("Total Orders\n" + totalOrders);
        btnPendingOrders.setText("Pending\n" + pendingOrders);
        btnLowStock.setText("Low Stock\n" + lowStock);

        // fetcingh data for overview
        int todayItems = Database.getTodayItemsSold();
        int completedToday = Database.getTodayCompletedOrders();

        lblOverviewItems.setText("● " + todayItems + " Items Sold Today");
        lblOverviewCompleted.setText("● " + completedToday + " Orders Completed Today");
        lblOverviewPending.setText("● " + pendingOrders + " Deliveries Pending");

        // Chart
        salesChart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        series.getData().add(new XYChart.Data<>(8, 0));
        series.getData().add(new XYChart.Data<>(12, todaySales));
        salesChart.getData().add(series);
    }

    //order processing
    private void setupOrdersTable() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colOrderItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        refreshOrders();

        btnConfirmOrder.setOnAction(e -> {
            OrderModel selected = ordersTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if(Database.updateOrderStatus(selected.getOrderId(), "Confirmed")) {
                    selected.setStatus("Confirmed");
                    ordersTable.refresh();
                    setupDashboardData(); // Update Overview immediately
                    showAlert("Success", "Order Confirmed");
                }
            } else showAlert("Warning", "Select an order");
        });

        btnGenerateBill.setOnAction(e -> generateBill());
    }

    private void refreshOrders() {
        ordersTable.setItems(FXCollections.observableArrayList(Database.getAllOrders()));
    }

    private void generateBill() {
        OrderModel selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Warning", "Select order"); return; }

        StringBuilder bill = new StringBuilder();
        bill.append("========== CAFEFLOW ==========\n");
        bill.append("Order: ").append(selected.getOrderId()).append("\n");
        bill.append("Customer: ").append(selected.getCustomer()).append("\n");
        bill.append("Items:\n");
        for(String s : selected.getItems().split(", ")) bill.append(" - ").append(s).append("\n");
        bill.append("------------------------------\n");
        bill.append(String.format("Total: $%.2f\n", selected.getTotal()));
        bill.append("Status: ").append(selected.getStatus()).append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receipt");
        alert.setHeaderText(null);
        TextArea area = new TextArea(bill.toString());
        area.setEditable(false);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    // menu
    private void setupMenuTable() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colItemStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        refreshMenu();

        btnAddItem.setOnAction(e -> {
            try {
                String name = txtItemName.getText();
                double price = Double.parseDouble(txtItemPrice.getText());
                int stock = Integer.parseInt(txtItemStock.getText());
                if (!name.isEmpty() && Database.addMenuItem(name, price, stock)) {
                    refreshMenu(); txtItemName.clear(); txtItemPrice.clear(); txtItemStock.clear();
                }
            } catch (Exception ex) { showAlert("Error", "Invalid input"); }
        });

        btnDeleteItem.setOnAction(e -> {
            MenuItem s = menuTable.getSelectionModel().getSelectedItem();
            if (s != null && Database.deleteMenuItem(s.getName())) refreshMenu();
        });

        btnIncreaseStock.setOnAction(e -> {
            MenuItem s = menuTable.getSelectionModel().getSelectedItem();
            if (s != null && Database.updateStock(s.getName(), s.getStock()+10)) {
                s.setStock(s.getStock()+10); menuTable.refresh();
            }
        });
    }

    private void refreshMenu() {
        menuTable.setItems(FXCollections.observableArrayList(Database.getMenu()));
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }
}