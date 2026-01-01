package com.example.cafeflowapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

public class customerController implements Initializable {

    @FXML private Button btnMenu;
    @FXML private Button btnPopular;
    @FXML private Button btnMyOrders;
    @FXML private Button btnProfile;

    @FXML private Label headerTitle;
    @FXML private GridPane menuGrid;
    @FXML private TextField searchField;
    @FXML private VBox centerContainer;
    @FXML private HBox headerRow;
    @FXML private ScrollPane menuScroll;

    @FXML private VBox cartSidebar;
    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Button orderButton;

    private List<MenuItem> allProducts = new ArrayList<>();
    private final List<MenuItem> currentCart = new ArrayList<>();
    private double currentTotal = 0.0;

    private Customer loggedInCustomer;

    public void setLoggedInCustomer(Customer customer) {
        this.loggedInCustomer = customer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMenuData();
        handleMenuClick();
        clearCart();
    }

    private void loadMenuData() {
        allProducts = Database.getMenu();
    }

    @FXML
    private void handleMenuClick() {
        setActiveButton(btnMenu);
        headerTitle.setText("Choose your breakfast");
        showCart(true);
        showMenuLayout();
        loadMenuData(); // Refresh DB data
        displayProducts(allProducts);
    }

    @FXML
    private void handlePopularClick() {
        setActiveButton(btnPopular);
        headerTitle.setText("Popular Items");
        showCart(true);
        showMenuLayout();
        loadMenuData();

        List<MenuItem> popularItems = allProducts.stream()
                .filter(p -> p.getPrice() > 4.00)
                .collect(Collectors.toList());
        displayProducts(popularItems);
    }

    @FXML
    private void handleMyOrdersClick() {
        setActiveButton(btnMyOrders);
        headerTitle.setText("My Past Orders");
        showCart(false);

        centerContainer.getChildren().setAll(headerRow);

        List<String> history = Database.getOrderHistory(loggedInCustomer.getEmail());

        if (history.isEmpty()) {
            Label emptyLabel = new Label("No past orders found.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #9e9e9e;");
            centerContainer.getChildren().add(emptyLabel);
        } else {
            VBox list = new VBox(10);
            list.setPadding(new Insets(10));
            for (String summary : history) {
                Label orderLabel = new Label(summary);
                orderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a3b30; -fx-background-color: #fff; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
                orderLabel.setMaxWidth(Double.MAX_VALUE);
                list.getChildren().add(orderLabel);
            }
            ScrollPane sp = new ScrollPane(list);
            sp.setFitToWidth(true);
            sp.setStyle("-fx-background-color: transparent;");
            VBox.setVgrow(sp, Priority.ALWAYS);
            centerContainer.getChildren().add(sp);
        }
    }

    @FXML
    private void handleProfileClick() {
        setActiveButton(btnProfile);
        headerTitle.setText("My Profile");
        showCart(false);

        centerContainer.getChildren().setAll(headerRow);

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(20));

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(70);
        form.getColumnConstraints().setAll(c1, c2);

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        if (loggedInCustomer != null) {
            nameField.setText(loggedInCustomer.getFullName());
            emailField.setText(loggedInCustomer.getEmail());
        }

        form.add(new Label("Name"), 0, 0); form.add(nameField, 1, 0);
        form.add(new Label("Email"), 0, 1); form.add(emailField, 1, 1);
        form.add(new Label("New Password"), 0, 2); form.add(passwordField, 1, 2);

        Button updateBtn = new Button("Update Profile");
        updateBtn.setStyle("-fx-background-color: #4A2C2A; -fx-text-fill: white;");
        updateBtn.setOnAction(e -> {
            String newName = nameField.getText();
            String newEmail = emailField.getText();
            String newPass = passwordField.getText();
            if(newPass.isEmpty()) newPass = loggedInCustomer.getPassword();

            try {
                if(Database.updateCustomer(loggedInCustomer.getId(), newName, newEmail, newPass)) {
                    loggedInCustomer.setFullName(newName);
                    loggedInCustomer.setEmail(newEmail);
                    loggedInCustomer.setPassword(newPass);
                    showAlert("Success", "Profile Updated");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });

        Button logoutBtn = new Button("Log out");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setOnAction(e -> logout());

        HBox actions = new HBox(10, updateBtn, logoutBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(20, form, actions);
        content.setMaxWidth(600);
        centerContainer.getChildren().add(content);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            displayProducts(allProducts);
            return;
        }
        String lower = query.toLowerCase();
        List<MenuItem> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
        displayProducts(filtered);
    }

    @FXML
    private void handleOrder() {
        if (currentCart.isEmpty()) {
            showAlert("Cart Empty", "Please add items to your cart.");
            return;
        }

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // bill information
        Map<String, Long> itemCounts = currentCart.stream()
                .collect(Collectors.groupingBy(MenuItem::getName, Collectors.counting()));

        StringBuilder itemsSummary = new StringBuilder();
        itemCounts.forEach((name, count) -> {
            itemsSummary.append(name).append(" x").append(count).append(", ");
        });
        if (itemsSummary.length() > 2) {
            itemsSummary.setLength(itemsSummary.length() - 2);
        }

        double tax = currentTotal * 0.05;
        double finalTotal = currentTotal + tax;

        // order placing in database
        boolean success = Database.placeOrder(
                orderId,
                loggedInCustomer.getFullName(),
                loggedInCustomer.getEmail(),
                itemsSummary.toString(),
                finalTotal
        );

        if (success) {
            //decrease stock
            for(MenuItem item : currentCart) {
                int newStock = item.getStock() - 1;
                if(newStock >= 0) Database.updateStock(item.getName(), newStock);
            }

            showAlert("Order Placed", "Order ID: " + orderId + "\nYour order has been sent to the kitchen!");
            clearCart();
            loadMenuData();
            if(btnMenu.getStyleClass().contains("nav-button-active")) displayProducts(allProducts);
        } else {
            showAlert("Error", "Could not place order.");
        }
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) centerContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showCart(boolean show) {
        cartSidebar.setVisible(show);
        cartSidebar.setManaged(show);
    }

    private void setActiveButton(Button activeButton) {
        resetButtonStyle(btnMenu);
        resetButtonStyle(btnPopular);
        resetButtonStyle(btnMyOrders);
        resetButtonStyle(btnProfile);
        if (!activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    private void resetButtonStyle(Button btn) {
        btn.getStyleClass().remove("nav-button-active");
        if (!btn.getStyleClass().contains("nav-button")) {
            btn.getStyleClass().add("nav-button");
        }
    }

    private void showMenuLayout() {
        centerContainer.getChildren().setAll(headerRow, menuScroll);
    }

    private void displayProducts(List<MenuItem> products) {
        menuGrid.getChildren().clear();
        int column = 0;
        int row = 1;

        for (MenuItem product : products) {
            VBox productCard = createProductCard(product);
            menuGrid.add(productCard, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(MenuItem product) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");

        Label imagePlaceholder = new Label("☕");
        imagePlaceholder.getStyleClass().add("product-image");

        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");

        Label priceLabel = new Label("$" + String.format("%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        Label stockLabel = new Label("Stock: " + product.getStock());
        stockLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

        Button addButton = new Button("Add to Order");
        addButton.getStyleClass().add("add-btn");

        if (product.getStock() <= 0) {
            addButton.setDisable(true);
            addButton.setText("Out of Stock");
        } else {
            addButton.setOnAction(e -> addToCart(product));
        }

        card.getChildren().addAll(imagePlaceholder, nameLabel, priceLabel, stockLabel, addButton);
        return card;
    }

    private void clearCart() {
        cartItemsContainer.getChildren().clear();
        currentCart.clear();
        currentTotal = 0.0;
        updateTotals(0);
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setId("emptyCartLabel");
        emptyLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-padding: 20;");
        cartItemsContainer.getChildren().add(emptyLabel);
    }

    private void addToCart(MenuItem product) {
        cartItemsContainer.getChildren().removeIf(node -> "emptyCartLabel".equals(node.getId()));

        currentCart.add(product);

        HBox cartItem = new HBox(10);
        cartItem.setAlignment(Pos.CENTER_LEFT);
        cartItem.getStyleClass().add("cart-item");

        Label name = new Label(product.getName());
        name.getStyleClass().add("cart-item-name");
        name.setMaxWidth(140);
        HBox.setHgrow(name, Priority.ALWAYS);

        Label price = new Label("$" + String.format("%.2f", product.getPrice()));
        price.getStyleClass().add("cart-item-price");

        Button removeBtn = new Button("x");
        removeBtn.getStyleClass().add("cart-remove-btn");
        removeBtn.setOnAction(e -> {
            cartItemsContainer.getChildren().remove(cartItem);
            currentCart.remove(product);
            updateTotals(-product.getPrice());
            if (cartItemsContainer.getChildren().isEmpty()) clearCart();
        });

        cartItem.getChildren().addAll(name, price, removeBtn);
        cartItemsContainer.getChildren().add(cartItem);

        updateTotals(product.getPrice());
    }

    private void updateTotals(double priceChange) {
        currentTotal += priceChange;
        if (currentTotal < 0) currentTotal = 0;

        double tax = currentTotal * 0.05;
        double finalTotal = currentTotal + tax;

        subtotalLabel.setText("$" + String.format("%.2f", currentTotal));
        taxLabel.setText("$" + String.format("%.2f", tax));
        totalLabel.setText("$" + String.format("%.2f", finalTotal));
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}