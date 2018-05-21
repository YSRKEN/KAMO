package com.ysrken.kamo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class Main extends Application {
    // ウィンドウにおけるマウスドラッグを開始した時の座標
    private double xOffset = 0, yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception{
        // FXMLファイルを読み込み
        final Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        // タイトルを設定
        primaryStage.setTitle(Utility.SOFTWARE_NAME);
        // 大きさを設定
        primaryStage.setScene(new Scene(root, 500, 300));
        // 最小の大きさを設定
        primaryStage.setMinWidth(350);
        primaryStage.setMinHeight(200);
        // 最前面設定
        primaryStage.setAlwaysOnTop(true);
        // ×ボタンを押した際に終了するように設定
        primaryStage.setOnCloseRequest(req -> Platform.exit());
        // マウスドラッグを設定
        root.setOnMousePressed(event -> {
            xOffset = primaryStage.getX() - event.getScreenX();
            yOffset = primaryStage.getY() - event.getScreenY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() + xOffset);
            primaryStage.setY(event.getScreenY() + yOffset);
        });
        // 表示
        primaryStage.show();
    }
    public static void main(String[] args) {
        //SceneRecognitionService.judgeScene(null);
        launch(args);
    }
}
