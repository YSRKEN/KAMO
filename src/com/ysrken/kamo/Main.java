package com.ysrken.kamo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
        primaryStage.setMinWidth(400);
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
        // ファイルドラッグを設定
        root.setOnDragOver(event -> {
            final var board = event.getDragboard();
            if (board.hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });
        root.setOnDragDropped(event -> {
            final var board = event.getDragboard();
            if (board.hasFiles()) {
                board.getFiles().forEach(file -> {
                    try {
                        final var BufferedImage = ImageIO.read(file);
                        final var scene = SceneRecognitionService.judgeScene(BufferedImage);
                        final var contentText = String.format("シーン判定：%s", scene.isEmpty() ? "不明" : scene);
                        Utility.showDialog(contentText, "画像認識結果");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        });
        // 表示
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
