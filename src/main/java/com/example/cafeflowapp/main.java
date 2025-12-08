package com.example.cafeflowapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("main-view.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );
        stage.setTitle("CafeFlow");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }


}
