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

public class IntroController {

    @FXML
    private Button iniciarButton;

    private WebSocketClient webSocketClient;
    /*
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
*/
    public void openLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
        Stage stage = (Stage) iniciarButton.getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(), 360, 300);
        stage.setTitle("Chat Application");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void pressIniciar(){
        try {
            openLogin();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
