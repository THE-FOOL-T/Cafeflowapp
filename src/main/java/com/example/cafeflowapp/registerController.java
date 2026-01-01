package com.example.cafeflowapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class registerController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = fullNameField.getText();
        String email = emailField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        try {
            boolean ok = Database.insertCustomer(name, email, pass);
            if (ok) {
                messageLabel.setText("Registered successfully. You can login now.");
                switchScene("main-view.fxml");
            } else {
                messageLabel.setText("Registration failed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Email already exists or database error.");
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        switchScene("main-view.fxml");
    }

    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) messageLabel.getScene().getWindow();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setResizable(true);
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Cannot navigate to " + fxmlPath);
        }
    }
}
