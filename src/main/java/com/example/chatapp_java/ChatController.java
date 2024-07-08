package com.example.chatapp_java;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField messageField;

    private WebSocketClient webSocketClient;

    @FXML
    public void initialize() {
        connectToWebSocket();
    }

    private void connectToWebSocket() {
        try {
            webSocketClient = new WebSocketClient(new URI("ws://localhost:8080")) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    chatArea.appendText("Connected to the server\n");
                }

                @Override
                public void onMessage(String message) {
                    chatArea.appendText(message + "\n");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    chatArea.appendText("Disconnected from the server\n");
                }

                @Override
                public void onError(Exception ex) {
                    chatArea.appendText("An error occurred: " + ex.getMessage() + "\n");
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
            webSocketClient.send(message);
            messageField.clear();
        }
    }
}
