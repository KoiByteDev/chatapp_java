package com.example.chatapp_java;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @FXML
    private Button iniciarButton;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("presentation.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 360, 300);
        stage.setTitle("Página de Presentación");
        stage.setScene(scene);
        stage.show();
    }

/*
    public void openLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));
        Stage stage = (Stage) iniciarButton.getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(), 360, 300);
        stage.setTitle("Chat Application");
        stage.setScene(scene);
        stage.show();
    }


    public void pressIniciar() throws IOException {
        openLogin();
    }
*/
    public static void main(String[] args) {
        launch();
    }
}