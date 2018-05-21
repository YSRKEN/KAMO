package com.ysrken.kamo;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainModel {
    /**
     * スクリーンショットボタンを押せるかどうかのフラグ
     */
    public BooleanProperty DisableSaveScreenshotFlg = new SimpleBooleanProperty(true);
    /**
     * 戦闘振り返り画面を開いているかどうかのフラグ
     */
    public BooleanProperty OpenBattleSceneReflectionFlg = new SimpleBooleanProperty(false);

    /**
     * MainViewのログ表示部分にログを追加するメソッド
     */
    private Consumer<String> addLogText;
    private BiConsumer<String, BufferedImage> setImage = null;
    private BiConsumer<String, String> setText = null;

    /**
     * 長い周期で行われるタスクを設定
     */
    private class LongIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(ScreenshotProvider.canGetScreenshot()){
                // ゲーム画面の位置が移動した際の処理
                if(ScreenshotProvider.isMovedPosition()){
                    addLogText.accept("【位置ズレ検知】");
                    addLogText.accept("自動で再取得を試みます...");
                    getPositionCommand();
                }
            }
        }
    }
    /**
     * 短い周期で行われるタスクを設定
     */
    private class ShortIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(ScreenshotProvider.canGetScreenshot()){
                final var frame = ScreenshotProvider.getScreenshot();
                final var scene = SceneRecognitionService.judgeScene(frame);
                if(scene.equals("昼戦後")){
                    Platform.runLater(() -> {
                        setImage.accept(scene, frame);
                        setText.accept(scene, Utility.getDateStringLong());
                    });
                }
            }
        }
    }

    /**
     * コンストラクタ
     * @param addLogText MainViewのログ表示部分にログを追加するメソッド
     */
    public MainModel(Consumer<String> addLogText){
        this.addLogText = addLogText;
        // 長周期で実行されるタイマー
        final var longIntervalTimer = new Timer();
        longIntervalTimer.schedule(new LongIntervalTask(), 0, 1000);
        // 短周期で実行されるタイマー
        final var shortIntervalTimer = new Timer();
        shortIntervalTimer.schedule(new ShortIntervalTask(), 0, 200);
    }
    /**
     * 終了コマンド
     */
    public void exitCommand(){
        Platform.exit();
    }
    /**
     * ゲーム座標を取得する
     */
    public void getPositionCommand(){
        addLogText.accept("【座標取得】");
        // 取得操作を行う
        final var getPositionFlg = ScreenshotProvider.trySearchGamePosition();
        // 取得に成功したか否かで処理を分ける
        if(getPositionFlg){
            // ゲーム座標を取得する
            final var rect = ScreenshotProvider.getPosition();
            // 取得したゲーム座標を記録する
            addLogText.accept(String.format("取得位置：(%d,%d)-%dx%d",
                    rect.x, rect.y, rect.width, rect.height));
            // スクリーンショットを使用可能にする
            DisableSaveScreenshotFlg.set(false);
        }else{
            addLogText.accept("座標取得：NG");
            // スクリーンショットを使用不可にする
            DisableSaveScreenshotFlg.set(true);
        }
    }
    /**
     * スクリーンショットを取得・保存する
     */
    public void saveScreenshotCommand(){
        addLogText.accept("【スクリーンショット】");
        if(ScreenshotProvider.canGetScreenshot()){
            final var screenShot = ScreenshotProvider.getScreenshot();
            final var fileName = String.format("%s.png", Utility.getDateStringLong());
            setImage.accept("昼戦後", screenShot);
            setText.accept("昼戦後", fileName);
            try {
                ImageIO.write(screenShot, "png", new File(String.format("pic\\%s", fileName)));
                addLogText.accept(String.format("ファイル名：%s", fileName));
            } catch (IOException e) {
                e.printStackTrace();
                addLogText.accept("エラー：スクリーンショットの保存に失敗しました。");
            }
        }else{
            addLogText.accept("エラー：スクリーンショットを取得できません。");
        }
    }
    /**
     * スクショの保存先であるpicフォルダを開く
     * パス指定の時点で明らかなように、Windowsにしか対応していない
     */
    public void openPicFolderCommand(){
        try {
            final var rt = Runtime.getRuntime();
            String cmd = String.format("explorer %s\\pic", System.getProperty("user.dir"));
            rt.exec(cmd);
        } catch (IOException e) {
            Utility.showDialog("picフォルダを開けませんでした。", "IOエラー", Alert.AlertType.ERROR);
        }
    }
    /**
     * 戦闘振り返り画面を開く
     */
    public void openBattleSceneReflectionCommand(){
        try {
            // 新しいウインドウを生成
            final var stage = new Stage();
            // ウィンドウの中身をFXMLから読み込み
            final var loader = new FXMLLoader(getClass().getResource("BattleSceneReflectionView.fxml"));
            final Parent root = loader.load();
            final BattleSceneReflectionController controller = loader.getController();
            this.setImage = (key, image) -> controller.setImage(key ,image);
            this.setText = (key, text) -> controller.setText(key, text);
            // タイトルを設定
            stage.setTitle("戦闘振り返り画面");
            // 大きさを設定
            stage.setScene(new Scene(root, 500, 300));
            // 最小の大きさを設定
            stage.setMinWidth(350);
            stage.setMinHeight(200);
            // 最前面設定
            stage.setAlwaysOnTop(true);
            // ×ボタンを押した際の設定
            stage.setOnCloseRequest(req -> {
                OpenBattleSceneReflectionFlg.set(false);
                this.setImage = null;
                this.setText = null;
            });
            // 新しいウインドウを表示
            stage.show();
            OpenBattleSceneReflectionFlg.set(true);
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showDialog("戦闘振り返り画面を開けませんでした。", "IOエラー", Alert.AlertType.ERROR);
        }
    }
    /**
     * ソフトウェアの更新が来ているかをチェックする
     */
    public void checkVersionCommand(){
        try {
            addLogText.accept("【更新チェック】");
            // 更新情報を表すテキストファイルをダウンロードする
            final var checkText = Utility.downloadTextData("https://raw.githubusercontent.com/YSRKEN/KAMO/master/version.txt");
            if(checkText.isEmpty())
                throw new IOException();
            // 更新文字列は「1,1.0.0」のような書式になっているはずなので確認する
            final var temp = checkText.split(",");
            if(temp.length < 2){
                throw new NumberFormatException();
            }
            // 情報を読み取っていく
            final var revision = Integer.parseInt(temp[0]);
            addLogText.accept(String.format("現在のバージョン：%s, リビジョン：%d",
                    Utility.SOFTWARE_VER, Utility.SOFTWARE_REVISION));
            addLogText.accept(String.format("最新のバージョン：%s, リビジョン：%d",
                    temp[1], revision));
            if(Utility.SOFTWARE_REVISION < revision){
                String message = String.format(
                        "より新しいバージョンが見つかりました。%n現在のバージョン：%s%n最新のバージョン：%s%nダウンロードサイトを開きますか？",
                        Utility.SOFTWARE_VER, temp[1]
                );
                final var openUrlFlg = Utility.showChoiceDialog(message, "更新チェック");
                if(openUrlFlg){
                    final var desktop = Desktop.getDesktop();
                    try{
                        desktop.browse(new URI(Utility.SOFTWARE_URL));
                    }catch( Exception e ){
                        e.printStackTrace();
                    }
                }
            }else{
                addLogText.accept("このソフトウェアは最新です。");
            }
        }catch(NumberFormatException | IOException e){
            e.printStackTrace();
            addLogText.accept("エラー：更新データを確認できませんでした。");
        }
    }
    /**
     * バージョン情報を表示する
     */
    public void aboutCommand(){
        final var contentText = String.format("ソフト名：%s%nバージョン：%s%n作者：%s",
                Utility.SOFTWARE_NAME,
                Utility.SOFTWARE_VER,
                Utility.SOFTWARE_AUTHOR
        );
        Utility.showDialog(contentText, "バージョン情報");
    }
}
