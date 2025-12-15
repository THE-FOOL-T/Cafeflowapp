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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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

    private final List<Product> allProducts = new ArrayList<>();
    private final List<String> pastOrders = new ArrayList<>();
    private double currentTotal = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadMockData();
        handleMenuClick();
        clearCart();
    }

    @FXML
    private void handleMenuClick() {
        setActiveButton(btnMenu);
        headerTitle.setText("Choose your breakfast");
        showCart(true);
        showMenuLayout();
        displayProducts(allProducts);
    }

    @FXML
    private void handlePopularClick() {
        setActiveButton(btnPopular);
        headerTitle.setText("Popular Items");
        showCart(true);
        showMenuLayout();
        List<Product> popularItems = allProducts.stream()
                .filter(p -> p.price > 3.50)
                .collect(Collectors.toList());
        displayProducts(popularItems);
    }

    @FXML
    private void handleMyOrdersClick() {
        setActiveButton(btnMyOrders);
        headerTitle.setText("My Past Orders");
        showCart(false);

        centerContainer.getChildren().setAll(headerRow);

        if (pastOrders.isEmpty()) {
            Label emptyLabel = new Label("No past orders found.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #9e9e9e;");
            centerContainer.getChildren().add(emptyLabel);
        } else {
            VBox list = new VBox(8);
            for (String summary : pastOrders) {
                Label orderLabel = new Label(summary);
                orderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a3b30;");
                list.getChildren().add(orderLabel);
            }
            centerContainer.getChildren().add(list);
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

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(70);
        form.getColumnConstraints().setAll(c1, c2);

        TextField nameField = new TextField();
        TextField usernameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        form.add(new Label("Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Username"), 0, 1);
        form.add(usernameField, 1, 1);
        form.add(new Label("Email"), 0, 2);
        form.add(emailField, 1, 2);
        form.add(new Label("New Password"), 0, 3);
        form.add(passwordField, 1, 3);
        form.add(new Label("Confirm Password"), 0, 4);
        form.add(confirmPasswordField, 1, 4);

        Button updateBtn = new Button("Update");
        updateBtn.setOnAction(e -> {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showAlert("Error", "Passwords do not match.");
            } else {
                showAlert("Profile", "Profile updated successfully.");
            }
        });

        Button logoutBtn = new Button("Log out");
        logoutBtn.setOnAction(e -> logout());

        HBox buttonsBox = new HBox(10, updateBtn, logoutBtn);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        centerContainer.getChildren().addAll(form, buttonsBox);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            displayProducts(allProducts);
            return;
        }
        String lower = query.toLowerCase();
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.name.toLowerCase().contains(lower))
                .collect(Collectors.toList());
        displayProducts(filtered);
    }

    @FXML
    private void handleOrder() {
        boolean emptyOrPlaceholder = cartItemsContainer.getChildren().isEmpty()
                || cartItemsContainer.getChildren().stream()
                .allMatch(n -> "emptyCartLabel".equals(n.getId()));
        if (emptyOrPlaceholder) {
            showAlert("Order", "Your cart is empty.");
            return;
        }

        String summary = "Order total " + totalLabel.getText();
        pastOrders.add(summary);

        showAlert("Order", "Order placed successfully!");
        clearCart();
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
            showAlert("Logout", "Unable to return to login screen.");
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

    private void loadMockData() {
        allProducts.add(new Product("Caramel Macchiato", 4.50, "img"));
        allProducts.add(new Product("Croissant", 2.50, "img"));
        allProducts.add(new Product("Iced Americano", 3.00, "img"));
        allProducts.add(new Product("Blueberry Muffin", 3.75, "img"));
        allProducts.add(new Product("Cappuccino", 4.00, "img"));
        allProducts.add(new Product("Bagel", 2.00, "img"));
    }

    private void showMenuLayout() {
        centerContainer.getChildren().setAll(headerRow, menuScroll);
    }

    private void displayProducts(List<Product> products) {
        menuGrid.getChildren().clear();
        int column = 0;
        int row = 1;

        for (Product product : products) {
            VBox productCard = createProductCard(product);
            menuGrid.add(productCard, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("product-card");

        Label imagePlaceholder = new Label("☕");
        imagePlaceholder.getStyleClass().add("product-image");

        Label nameLabel = new Label(product.name);
        nameLabel.getStyleClass().add("product-name");

        Label priceLabel = new Label("$" + String.format("%.2f", product.price));
        priceLabel.getStyleClass().add("product-price");

        Button addButton = new Button("Add to Order");
        addButton.getStyleClass().add("add-btn");
        addButton.setOnAction(e -> addToCart(product));

        card.getChildren().addAll(imagePlaceholder, nameLabel, priceLabel, addButton);
        return card;
    }

    private void clearCart() {
        cartItemsContainer.getChildren().clear();
        currentTotal = 0.0;
        updateTotals(0);
        Label emptyLabel = new Label("Your cart is empty");
        emptyLabel.setId("emptyCartLabel");
        emptyLabel.setStyle("-fx-text-fill: #9e9e9e; -fx-padding: 20;");
        cartItemsContainer.getChildren().add(emptyLabel);
    }

    private void addToCart(Product product) {
        cartItemsContainer.getChildren().removeIf(node -> "emptyCartLabel".equals(node.getId()));

        HBox cartItem = new HBox(10);
        cartItem.setAlignment(Pos.CENTER_LEFT);
        cartItem.getStyleClass().add("cart-item");

        Label name = new Label(product.name);
        name.getStyleClass().add("cart-item-name");
        name.setMaxWidth(140);
        HBox.setHgrow(name, Priority.ALWAYS);

        Label price = new Label("$" + String.format("%.2f", product.price));
        price.getStyleClass().add("cart-item-price");

        Button removeBtn = new Button("x");
        removeBtn.getStyleClass().add("cart-remove-btn");
        removeBtn.setOnAction(e -> {
            cartItemsContainer.getChildren().remove(cartItem);
            updateTotals(-product.price);
            if (cartItemsContainer.getChildren().isEmpty()) clearCart();
        });

        cartItem.getChildren().addAll(name, price, removeBtn);
        cartItemsContainer.getChildren().add(cartItem);

        updateTotals(product.price);
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

    static class Product {
        String name;
        double price;
        String imageUrl;

        Product(String name, double price, String imageUrl) {
            this.name = name;
            this.price = price;
            this.imageUrl = imageUrl;
        }
    }
}
