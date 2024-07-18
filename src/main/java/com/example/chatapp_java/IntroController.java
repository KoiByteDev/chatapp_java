package com.example.chatapp_java;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import org.java_websocket.client.WebSocketClient;

import java.io.IOException;

public class IntroController {

    @FXML
    private ImageView imView;

    @FXML
    private Button iniciarButton;

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
