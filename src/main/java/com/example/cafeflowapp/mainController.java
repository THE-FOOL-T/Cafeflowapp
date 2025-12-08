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

public class mainController {

    @FXML
    private Button adminSideButton;

    @FXML
    private Button userSideButton;

    @FXML
    private Label roleTitleLabel;

    @FXML
    private TextField usernameField;

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

        boolean valid = fakeValidate(username, password, currentRole);
        if (!valid) {
            messageLabel.setText("Invalid " + currentRole + " credentials.");
            return;
        }

        String fxmlPath = "Admin".equals(currentRole)? "admin.fxml" :"customer.fxml";

        switchScene(fxmlPath);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        switchScene("register.fxml");
    }

    private boolean fakeValidate(String username, String password, String role) {
        if ("Admin".equals(role)) {
            return "admin".equals(username) && "admin123".equals(password);
        } else {
            return password.length() >= 3;
        }
    }

    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Scene scene = new Scene(root);
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
