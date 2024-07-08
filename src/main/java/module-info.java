module com.example.chatapp_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.java_websocket;
    requires org.glassfish.tyrus.server;
    requires org.glassfish.tyrus.client;

    opens com.example.chatapp_java to javafx.fxml;
    exports com.example.chatapp_java;
}