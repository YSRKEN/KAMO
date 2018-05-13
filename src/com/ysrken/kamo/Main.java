package com.ysrken.kamo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        primaryStage.setTitle(Utility.getSoftwareName());
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.setMinWidth(350);
        primaryStage.setMinHeight(200);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
