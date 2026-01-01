package com.example.cafeflowapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class mainController {

    @FXML
    private Button adminSideButton;

    @FXML
    private Button userSideButton;

    @FXML
    private Label roleTitleLabel;

    @FXML
    private TextField usernameField; // for customer: email

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private ImageView logoImage;

    @FXML
    private HBox registerBox;

    private String currentRole = "Customer";

    @FXML
    private void initialize() {
        Image img = new Image(getClass().getResourceAsStream("/images/cafeflow_logo.png"));
        logoImage.setImage(img);
        highlightRoleButtons();
        updateRegisterVisibility();
    }

    @FXML
    private void selectAdmin(ActionEvent event) {
        currentRole = "Admin";
        highlightRoleButtons();
        updateRegisterVisibility();
    }

    @FXML
    private void selectCustomer(ActionEvent event) {
        currentRole = "Customer";
        highlightRoleButtons();
        updateRegisterVisibility();
    }

    private void highlightRoleButtons() {
        String selected =
                "-fx-background-color: #4A2C2A; -fx-text-fill: #FAF0E6; -fx-background-radius: 20; -fx-padding: 8 16;";
        String unselected =
                "-fx-background-color: transparent; -fx-text-fill: #4A2C2A; -fx-border-color: #4A2C2A; " +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 16;";

        if ("Admin".equals(currentRole)) {
            adminSideButton.setStyle(selected);
            userSideButton.setStyle(unselected);
        } else {
            userSideButton.setStyle(selected);
            adminSideButton.setStyle(unselected);
        }

        roleTitleLabel.setText(currentRole + " Login");
        usernameField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }

    private void updateRegisterVisibility() {
        boolean show = "Customer".equals(currentRole);
        registerBox.setVisible(show);
        registerBox.setManaged(show);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and password are required.");
            return;
        }

        try {
            if ("Admin".equals(currentRole)) {
                if (!("admin".equals(username) && "admin123".equals(password))) {
                    messageLabel.setText("Invalid Admin credentials.");
                    return;
                }
                switchScene("admin.fxml", null);
            } else {

                Customer customer = Database.findCustomer(username, password);
                if (customer == null) {
                    messageLabel.setText("Invalid Customer credentials.");
                    return;
                }
                switchScene("customer.fxml", customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Database error.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        switchScene("register.fxml", null);
    }

    private void switchScene(String fxmlPath, Customer customer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (customer != null && "customer.fxml".equals(fxmlPath)) {
                customerController controller = loader.getController();
                controller.setLoggedInCustomer(customer);
            }

            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("CafeFlow - " + currentRole);
            stage.setResizable(true);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Cannot open " + fxmlPath);
        }
    }
}
