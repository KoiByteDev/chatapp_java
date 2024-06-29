module com.example.chatapp_java {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.chatapp_java to javafx.fxml;
    exports com.example.chatapp_java;
}
