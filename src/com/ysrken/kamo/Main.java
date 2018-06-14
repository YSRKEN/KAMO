package com.ysrken.kamo;

import com.ysrken.kamo.Controller.MainController;
import com.ysrken.kamo.Service.SceneRecognitionService;
import com.ysrken.kamo.Service.SettingsStore;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Main extends Application {
    // ウィンドウにおけるマウスドラッグを開始した時の座標
    private double xOffset = 0, yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception{
        // FXMLファイルを読み込み
        final var loader = new FXMLLoader(
                ClassLoader.getSystemResource("com/ysrken/kamo/View/MainView.fxml"));
        final Parent root = loader.load();
        final MainController controller = loader.getController();
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
        primaryStage.setOnCloseRequest(req -> {
            Platform.exit();
            System.exit(0);
        });
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
                        final var image = ImageIO.read(file);
                        SceneRecognitionService.testSceneRecognition(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        });
        // ウィンドウの位置を復元
        if(SettingsStore.SaveWindowPositionFlg.get()){
            final var rect = SettingsStore.MainView.get();
            if(rect.x != Integer.MAX_VALUE){
                primaryStage.setX(rect.x);
            }else{
                rect.x = (int)primaryStage.getX();
            }
            if(rect.y != Double.MAX_VALUE){
                primaryStage.setY(rect.y);
            }else{
                rect.y = (int)primaryStage.getY();
            }
            if(rect.width != Double.MAX_VALUE){
                primaryStage.setWidth(rect.width);
            }else{
                rect.width = (int)primaryStage.getWidth();
            }
            if(rect.height != Double.MAX_VALUE){
                primaryStage.setHeight(rect.height);
            }else{
                rect.height = (int)primaryStage.getHeight();
            }
        }
        primaryStage.xProperty().addListener((ob, o, n) -> {
            SettingsStore.MainView.set(new Rectangle((int)primaryStage.getX(), (int)primaryStage.getY(), (int)primaryStage.getWidth(), (int)primaryStage.getHeight()));
        });
        primaryStage.yProperty().addListener((ob, o, n) -> {
            SettingsStore.MainView.set(new Rectangle((int)primaryStage.getX(), (int)primaryStage.getY(), (int)primaryStage.getWidth(), (int)primaryStage.getHeight()));
        });
        primaryStage.widthProperty().addListener((ob, o, n) -> {
            SettingsStore.MainView.set(new Rectangle((int)primaryStage.getX(), (int)primaryStage.getY(), (int)primaryStage.getWidth(), (int)primaryStage.getHeight()));
        });
        primaryStage.heightProperty().addListener((ob, o, n) -> {
            SettingsStore.MainView.set(new Rectangle((int)primaryStage.getX(), (int)primaryStage.getY(), (int)primaryStage.getWidth(), (int)primaryStage.getHeight()));
        });
        // 表示
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
