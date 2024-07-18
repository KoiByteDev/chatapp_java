package com.example.chatapp_java;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ChatController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageField;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private StackPane rootPane;

    @FXML 
    private Button addFriendsButton;

    private VBox friendsContainer;
    private StackPane overlayPane;
    private VBox friendList;

    private WebSocketClient webSocketClient;
    private String username;

    @FXML
    private VBox groupsContainer;

    private String currentChatPartner = null;

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
                public void onMessage(String message) {
                    Platform.runLater(() -> {
                        if (message.startsWith("foundFriends😸:")) {
                            handleFriendsResponse(message.substring(15));
                        } else if (message.startsWith("friendAdded,")) {
                            handleFriendAdded(message.substring(12));
                        } else if (message.startsWith("friendList,")) {
                            handleFriendList(message.substring(11));
                        } else if (message.startsWith("privateMsg,")) {
                            handlePrivateMessage(message.substring(11));
                        } else if (message.startsWith("nofrendxd")) {
                            handleFriendsResponse("");
                        } else {
                            handleChatMessage(message);
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

    private void handleFriendList(String friendListData) {
        groupsContainer.getChildren().clear();
        List<String> friends = Arrays.asList(friendListData.split(","));
        for (String friend : friends) {
            addFriendToGroupList(friend);
        }
    }

    private void addFriendToGroupList(String friendUsername) {
        Button chatButton = new Button("Chat");
        chatButton.setStyle("-fx-background-color: #0e1531; -fx-text-fill: lightgray;");
        chatButton.setOnAction(e -> openPrivateChat(friendUsername));

        Text text = new Text(friendUsername);
        text.setStyle("-fx-fill: white;");

        HBox friendBox = new HBox(text, chatButton);
        friendBox.setStyle("-fx-background-color: #161d39; -fx-padding: 10; -fx-border-color: #161d39; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-margin: 2;");
        friendBox.setMaxWidth(Double.MAX_VALUE);
        friendBox.setMinHeight(Region.USE_PREF_SIZE);

        groupsContainer.getChildren().add(friendBox);
    }

    private void handlePrivateMessage(String message) {
        String[] parts = message.split(",", 2);
        String sender = parts[0];
        String chatMessage = parts[1];

        if (sender.equals(currentChatPartner) || sender.equals(username)) {
            addMessageToChat(chatMessage);
        }
    }

    private void handleFriendAdded(String friendUsername) {
        Button chatButton = new Button("Chat");
        chatButton.setStyle("-fx-background-color: #0e1531; -fx-text-fill: lightgray;");
        chatButton.setOnAction(e -> openPrivateChat(friendUsername));

        Text text = new Text(friendUsername);
        text.setStyle("-fx-fill: white;");

        HBox friendBox = new HBox(text, chatButton);
        friendBox.setStyle("-fx-background-color: #161d39; -fx-padding: 10; -fx-border-color: #161d39; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-margin: 2;");
        friendBox.setMaxWidth(Double.MAX_VALUE);
        friendBox.setMinHeight(Region.USE_PREF_SIZE);

        groupsContainer.getChildren().add(friendBox);
    }

    private void addFriend(String friendUsername) {
        webSocketClient.send("addFriend," + username + "," + friendUsername);
    }

    private void openPrivateChat(String friendUsername) {
        currentChatPartner = friendUsername;
        messagesContainer.getChildren().clear();
        webSocketClient.send("fetchFriendMessages," + username + "," + friendUsername);
        addMessageToChat("Iniciando chat privado con " + friendUsername);
    }

    private void handleChatMessage(String message) {
        if (message.startsWith("messagesFor,")) {
            String[] parts = message.split(",", 3);
            if (parts.length >= 3) {
                String friendUsername = parts[1];
                String chatMessages = parts[2];
                String[] messageArray = chatMessages.split("&");
                for (String msg : messageArray) {
                    addMessageToChat(msg);
                }
            }
        } else {
            webSocketClient.send("fetchFriendList," + username);
            String[] messageArray = message.split("&");
            for (String msg : messageArray) {
                addMessageToChat(msg);
            }
        }
    }

    private void addMessageToChat(String message) {
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

    @FXML
    public void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty() && webSocketClient != null && webSocketClient.isOpen()) {
            if (currentChatPartner != null) {
                webSocketClient.send("privateMsg," + username + "," + currentChatPartner + "," + message);
            } else {
                String mensaje = username + ": " + message;
                webSocketClient.send("msg," + mensaje);
            }
            Platform.runLater(() -> {
                scrollPane.layout();
                scrollPane.setVvalue(1.0);
            });
            messageField.clear();
        }
    }

    @FXML
    public void openFriendsMenu() {
        if (friendsContainer == null) {
            overlayPane = new StackPane();
            overlayPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
            StackPane.setAlignment(overlayPane, Pos.CENTER);

            friendsContainer = new VBox();
            friendsContainer.setStyle("-fx-background-color: #0e1531; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
            friendsContainer.setSpacing(10);
            friendsContainer.setMinWidth(300);
            friendsContainer.setMinHeight(400);

            HBox header = new HBox();
            header.setStyle("-fx-alignment: top-right;");
            Button closeButton = new Button("X");
            closeButton.setStyle("-fx-background-color: #0e1531; -fx-text-fill: lightgray;");
            closeButton.setOnAction(e -> {
                rootPane.getChildren().removeAll(friendsContainer, overlayPane);
                friendsContainer = null;
                overlayPane = null;
            });
            header.getChildren().add(closeButton);

            TextField addFriendField = new TextField();
            addFriendField.setPromptText("Añadir amigo...");
            addFriendField.setStyle("-fx-background-color: #161d39; -fx-text-fill: #d3d3d3;");
            HBox.setHgrow(addFriendField, Priority.ALWAYS);

            friendList = new VBox();
            friendList.setSpacing(5);
            VBox.setVgrow(friendList, Priority.ALWAYS);

            Button submitButton = new Button("⮚");
            submitButton.setStyle("-fx-background-color: #0e1531; -fx-text-fill: lightgray;");
            submitButton.setOnAction(e -> {
                String friendName = addFriendField.getText();
                if (!friendName.isEmpty() && webSocketClient != null && webSocketClient.isOpen()) {
                    webSocketClient.send("searchForFrendPls🥺," + friendName);
                }
            });

            HBox inputContainer = new HBox(addFriendField, submitButton);
            inputContainer.setSpacing(5);
            HBox.setHgrow(inputContainer, Priority.ALWAYS);

            friendsContainer.getChildren().addAll(header, friendList, inputContainer);

            StackPane.setMargin(friendsContainer, new Insets(200, 200, 200, 200));
            overlayPane.getChildren().add(friendsContainer);
            rootPane.getChildren().add(overlayPane);
        } else {
            rootPane.getChildren().removeAll(friendsContainer, overlayPane);
            friendsContainer = null;
            overlayPane = null;
        }
    }

    private void handleFriendsResponse(String friendsData) {
        friendList.getChildren().clear();
        List<String> foundUsers = Arrays.asList(friendsData.split(","));
        
        if (friendsData.isEmpty()) {
            addFriendToList("No se encontraron usuarios (Skill Issue 👻👻👻🔥🙏😹)", false);
        } else {
            for (String name : foundUsers) {
                addFriendToList(name, true);
            }
        }
    }

    private void addFriendToList(String name, Boolean found) {
        HBox friendBox = new HBox();
        friendBox.setStyle("-fx-background-color: #161d39; -fx-padding: 10; -fx-border-color: #161d39; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-margin: 2;");

        Text text = new Text(name);
        text.setStyle("-fx-fill: white;");

        HBox espacio = new HBox();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        if (found) {
            Button addButton = new Button("Añadir amigo");
            addButton.setStyle("-fx-background-color: #0e1531; -fx-text-fill: lightgray;");
            addButton.setOnAction(e -> {
                addFriend(name);
            });

            friendBox.getChildren().addAll(text, espacio, addButton);
            friendBox.setMaxWidth(Double.MAX_VALUE);
            friendBox.setMinHeight(Region.USE_PREF_SIZE);

            friendList.getChildren().add(friendBox);
        } else {
            friendBox.getChildren().addAll(text, espacio);
            friendBox.setMaxWidth(Double.MAX_VALUE);
            friendBox.setMinHeight(Region.USE_PREF_SIZE);

            friendList.getChildren().add(friendBox);
        }
    }

    @FXML
    public void toGeneral() {
        webSocketClient.send("fetchMessages");
        messagesContainer.getChildren().clear();
    }
}
