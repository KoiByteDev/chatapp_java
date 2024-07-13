package com.example.chatapp_java;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
                        webSocketClient.send("join," + username);
                    });
                }

                @Override
                public void onMessage(String messages) {
                    Platform.runLater(() -> {

                        System.out.println("Messages before formatting:" + messages);

                        String[] messageArray = messages.split("&");

                        System.out.println("Messages after formatting:" + messageArray);

                        for (String message : messageArray) {
                            System.out.println("Received message:" + message);

                            HBox messageBox = new HBox();
                            messageBox.setStyle("-fx-background-color: #0e1531; -fx-padding: 10; -fx-border-color: #0e1531; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-margin: 2; -fx-max-width: 100px;");

                            Text text = new Text(message);
                            text.setStyle("-fx-fill: white;");

                            messageBox.getChildren().add(text);
                            messageBox.setMaxWidth(Double.MAX_VALUE);
                            messageBox.setMinHeight(Region.USE_PREF_SIZE);

                            messagesContainer.getChildren().add(messageBox);
                            scrollPane.layout();
                            scrollPane.setVvalue(1.0);
                        }
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
            webSocketClient.send("msg," + mensaje);
            Platform.runLater(() -> {
                scrollPane.layout();
                scrollPane.setVvalue(1.0);
            });
            messageField.clear();
        }
    }
}
