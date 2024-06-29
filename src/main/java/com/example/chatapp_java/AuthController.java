package com.example.chatapp_java;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    @FXML
    private Text switchAuthText;
    private boolean isLoginMode = true;

    @FXML
    protected void initialize() {
        switchAuthText.setOnMouseClicked(event -> switchAuthMode());
    }

    @FXML
    protected void handleAuthAction(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isLoginMode) {
            if (authenticate(username, password)) {
                loadChatScreen();
            } else {
                System.out.println("Login failed!");
            }
        } else {
            if (signUp(username, password)) {
                System.out.println("Signup successful! Please log in.");
                switchAuthMode();
            } else {
                System.out.println("Signup failed!");
            }
        }
    }

    private void switchAuthMode() {
        isLoginMode = !isLoginMode;
        authButton.setText(isLoginMode ? "Login" : "Sign Up");
        switchAuthText.setText(isLoginMode ? "Don't have an account? Sign Up" : "Already have an account? Login");
    }

    private boolean authenticate(String username, String password) {
        // Dummy authentication logic
        return username.equals("user") && password.equals("pass");
    }

    private boolean signUp(String username, String password) {
        // Dummy sign up logic
        return true;
    }

    private void loadChatScreen() throws IOException {
        Stage stage = (Stage) authButton.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/example/chatapp_java/chat.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setScene(scene);
    }
}
