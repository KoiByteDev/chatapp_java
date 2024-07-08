package com.example.chatapp_java;

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

    @FXML
    public void handleAuthAction() {
        // For now, just open the chat screen
        openChatScreen();
    }

    private void openChatScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("chat.fxml"));
            Stage stage = (Stage) authButton.getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load(), 320, 240);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
