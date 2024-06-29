package com.example.chatapp_java;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ChatController {
    @FXML
    private TextArea chatArea;

    @FXML
    protected void initialize() {
        chatArea.appendText("Welcome to the public chat!\n");
    }
}
