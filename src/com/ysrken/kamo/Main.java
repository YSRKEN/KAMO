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
            if(SettingsStore.MainViewX.get() != Double.MAX_VALUE){
                primaryStage.setX(SettingsStore.MainViewX.get());
            }else{
                SettingsStore.MainViewX.set(primaryStage.getX());
            }
            if(SettingsStore.MainViewY.get() != Double.MAX_VALUE){
                primaryStage.setY(SettingsStore.MainViewY.get());
            }else{
                SettingsStore.MainViewY.set(primaryStage.getY());
            }
            if(SettingsStore.MainViewW.get() != Double.MAX_VALUE){
                primaryStage.setWidth(SettingsStore.MainViewW.get());
            }else{
                SettingsStore.MainViewW.set(primaryStage.getWidth());
            }
            if(SettingsStore.MainViewH.get() != Double.MAX_VALUE){
                primaryStage.setHeight(SettingsStore.MainViewH.get());
            }else{
                SettingsStore.MainViewH.set(primaryStage.getHeight());
            }
        }
        SettingsStore.MainViewX.bind(primaryStage.xProperty());
        SettingsStore.MainViewY.bind(primaryStage.yProperty());
        SettingsStore.MainViewW.bind(primaryStage.widthProperty());
        SettingsStore.MainViewH.bind(primaryStage.heightProperty());
        // 表示
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
