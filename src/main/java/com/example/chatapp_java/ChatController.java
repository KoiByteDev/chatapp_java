package com.example.chatapp_java;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageField;

    @FXML
    private ScrollPane scrollPane;

    private WebSocketClient webSocketClient;
    private String username;

    @FXML
    public void initialize() {
        connectToWebSocket();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void connectToWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI("ws://localhost:8080")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Platform.runLater(() -> {
                        messagesContainer.getChildren().add(new Text("Conectado al servidor"));
                        webSocketClient.send("join," + username);
                    });
                }

                @Override
                public void onMessage(String message) {
                    Platform.runLater(() -> {
                        messagesContainer.getChildren().add(new Text(message));
                        scrollPane.layout();
                        scrollPane.setVvalue(1.0);  // Scroll to the bottom
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Platform.runLater(() -> {
                        messagesContainer.getChildren().add(new Text(username + " ha abandonado la conversación"));
                        webSocketClient.send("leave," + username);
                    });
                }

                @Override
                public void onError(Exception ex) {
                    Platform.runLater(() -> messagesContainer.getChildren().add(new Text("System: Ocurrió un error: " + ex.getMessage())));
                }
            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty() && webSocketClient != null && webSocketClient.isOpen()) {
            String mensaje = username + ": " + message;
            webSocketClient.send(mensaje);
            Platform.runLater(() -> {
                scrollPane.layout();
                scrollPane.setVvalue(1.0);
            });
            messageField.clear();
        }
    }
}
