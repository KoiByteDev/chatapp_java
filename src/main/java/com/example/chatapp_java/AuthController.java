package com.example.chatapp_java;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button authButton;

    @FXML
    private Text switchAuthText;

    @FXML
    private Label errorLabel;

    private WebSocketClient webSocketClient;
    private boolean isRegisterMode = false;
    private String username;

    @FXML
    public void initialize() {
        connectToWebSocket();
    }

    private void connectToWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI("ws://localhost:8080")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to the server");
                }

                @Override
                public void onMessage(String message) {
                    Platform.runLater(() -> handleServerResponse(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from the server");
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("An error occurred: " + ex.getMessage());
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAuthAction() {
        username = usernameField.getText();
        String password = passwordField.getText();
        if (isRegisterMode) {
            String email = emailField.getText();
            User user = new User(username, email);
            if (!username.isEmpty() && !password.isEmpty() && !email.isEmpty()) {
                webSocketClient.send("register," + user.getUsername() + "," + password + "," + user.getEmail());
            } else {
                showError("Por favor complete todos los campos.");
            }
        } else {
            if (!username.isEmpty() && !password.isEmpty()) {
                webSocketClient.send("login," + username + "," + password);
            } else {
                showError("Por favor complete todos los campos.");
            }
        }
    }

    @FXML
    public void switchAuthMode(MouseEvent event) {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            authButton.setText("Crear Cuenta");
            switchAuthText.setText("Ya tiene una cuenta? Iniciar Sesión.");
            emailField.setVisible(true);
        } else {
            authButton.setText("Iniciar Sesión");
            switchAuthText.setText("No tiene una cuenta? Crear Cuenta.");
            emailField.setVisible(false);
        }
    }

    private void openChatScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("chat.fxml"));
            Stage stage = (Stage) authButton.getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load(), 854, 480);
            ChatController chatController = fxmlLoader.getController();
            chatController.setUsername(usernameField.getText());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleServerResponse(String message) {
        if (message.equals("Registro exitoso!") || message.equals("Inicio de sesión exitoso!")) {
            openChatScreen();
        } else if (message.startsWith("friendList,")) {
            openChatScreen();
        } else {
            showError(message);
        }
    }

    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(true);
    }
}
