package com.ysrken.kamo.Model;

import com.ysrken.kamo.Controller.BattleSceneReflectionController;
import com.ysrken.kamo.Controller.SceneHelperController;
import com.ysrken.kamo.Controller.TimerController;
import com.ysrken.kamo.Service.*;
import com.ysrken.kamo.Utility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MainModel {
    /** スクリーンショットボタンを押せるかどうかのフラグ */
    public BooleanProperty DisableSaveScreenshotFlg = new SimpleBooleanProperty(true);
    /** 戦闘振り返り画面を開いているかどうかのフラグ */
    public BooleanProperty OpenBattleSceneReflectionFlg = new SimpleBooleanProperty(false);
    /** 各種タイマー画面を開いているかどうかのフラグ */
    public BooleanProperty OpenTimerFlg = new SimpleBooleanProperty(false);
    /**
     * 画像認識支援画面を開いているかどうかのフラグ
     */
    public BooleanProperty OpenSceneHelperFlg = new SimpleBooleanProperty(false);
    /** シーン情報 */
    public StringProperty NowSceneText = new SimpleStringProperty("シーン判定：[不明]");
    /**
     * 自動で座標を取得し直すか？
     */
    public BooleanProperty AutoGetPositionFlg = SettingsStore.AutoGetPositionFlg;
    /** スクショで提督名を隠すか？ */
    public BooleanProperty BlindNameTextFlg = SettingsStore.BlindNameTextFlg;
    /** 座標取得で特殊な方式を使用するか？ */
    public BooleanProperty SpecialGetPosFlg = SettingsStore.SpecialGetPosFlg;

    /**
     * MainViewのログ表示部分にログを追加するメソッド
     */
    private Consumer<String> addLogText;
    private BiConsumer<String, BufferedImage> setImage = null;
    private BiConsumer<String, String> setText = null;
    private Set<String> battleSceneSet = null;
    private BiConsumer<Date, Integer> setExpTimer = null;
    private BiConsumer<String, Integer> setExpInfo = null;
    private Runnable refreshExpTimerString = null;

    /**
     * 長い周期で行われるタスクを設定
     */
    private class LongIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(ScreenshotService.canGetScreenshot()){
                // ゲーム画面の位置が移動した際の処理
                if(ScreenshotService.isMovedPosition()){
                    addLogText.accept("【位置ズレ検知】");
                    addLogText.accept("自動で再取得を試みます...");
                    getPositionCommand();
                }
            }else if(AutoGetPositionFlg.get()){
                addLogText.accept("【自動座標認識】");
                addLogText.accept("自動で再取得を試みます...");
                getPositionCommand();
            }
        }
    }
    /**
     * 短い周期で行われるタスクを設定
     */
    private class ShortIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(ScreenshotService.canGetScreenshot()){
                // 画像を取得
                final var frame = ScreenshotService.getScreenshot();
                // シーンを読み取り、結果をメイン画面に表示する
                final var scene = SceneRecognitionService.judgeScene(frame);
                final var isNearlyHomeFlg = SceneRecognitionService.isNearlyHomeScene(frame);
                setSceneText(String.format("シーン判定：%s%s",
                        scene.isEmpty() ? "[不明]" : scene,
                        isNearlyHomeFlg ? "*" : ""));
                // 戦闘振り返り機能が有効になっていた際、特定シーンの画像を転送する
                if(OpenBattleSceneReflectionFlg.get()){
                    if(battleSceneSet.contains(scene)){
                        setImage.accept(scene, frame);
                        setText.accept(scene, Utility.getDateStringLong());
                    }
                }
                // 各種タイマー機能が有効になっていた際、画像認識により時刻を随時更新する
                if(OpenTimerFlg.get()){
                    if(scene.equals("遠征一覧") || scene.equals("遠征中止")){
                        final var duration = CharacterRecognitionService.getExpeditionRemainingTime(frame);
                        if(setExpTimer != null && setExpInfo != null && duration >= 0){
                            final var expeditionId = CharacterRecognitionService.getSelectedExpeditionId(frame);
                            final var fieetIds = CharacterRecognitionService.getExpeditionFleetId(frame);
                            for(var pair : fieetIds.entrySet()){
                                if(pair.getValue().equals(expeditionId)){
                                    setExpTimer.accept(new Date(new Date().getTime() + duration * 1000), pair.getKey() - 2);
                                    setExpInfo.accept(CharacterRecognitionService.getExpeditionNameById(pair.getValue()), pair.getKey() - 2);
                                    break;
                                }
                            }
                        }
                    }
                    if(refreshExpTimerString != null){
                        refreshExpTimerString.run();
                    }
                }
            }
        }
    }
    /** シーン情報の表示を更新する */
    private void setSceneText(String str){
        Platform.runLater(() -> NowSceneText.set(str));
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
        final var getPositionFlg = ScreenshotService.trySearchGamePosition(addLogText);
        // 取得に成功したか否かで処理を分ける
        if(getPositionFlg){
            // ゲーム座標を取得する
            final var rect = ScreenshotService.getPosition();
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
    /** スクリーンショットを取得・保存する */
    public void saveScreenshotCommand(){
        addLogText.accept("【スクリーンショット】");
        if(ScreenshotService.canGetScreenshot()){
            final var screenShot = ScreenshotService.getScreenshot();
            final var processedImage = PictureProcessingService.getProcessedImage(screenShot);
            final var fileName = String.format("%s.png", Utility.getDateStringLong());
            try {
                ImageIO.write(processedImage, "png", new File(String.format("pic\\%s", fileName)));
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
    /** 戦闘振り返り画面を開く */
    public void openBattleSceneReflectionCommand(){
        try {
            // 新しいウインドウを生成
            final var stage = new Stage();
            // ウィンドウの中身をFXMLから読み込み
            final var loader = new FXMLLoader(
                    ClassLoader.getSystemResource("com/ysrken/kamo/View/BattleSceneReflectionView.fxml"));
            final Parent root = loader.load();
            final BattleSceneReflectionController controller = loader.getController();
            this.setImage = (key, image) -> controller.setImage(key ,image);
            this.setText = (key, text) -> controller.setText(key, text);
            battleSceneSet = controller.getBattleSceneSet();
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
    /** 各種タイマー画面を開く */
    public void openTimerCommand(){
        try {
            // 新しいウインドウを生成
            final var stage = new Stage();
            // ウィンドウの中身をFXMLから読み込み
            final var loader = new FXMLLoader(
                    ClassLoader.getSystemResource("com/ysrken/kamo/View/TimerView.fxml"));
            final Parent root = loader.load();
            final TimerController controller = loader.getController();
            setExpTimer = (date, index) -> controller.setExpTimer(date, index);
            setExpInfo = (info, index) -> controller.setExpInfo(info, index);
            refreshExpTimerString = (() -> controller.refreshExpTimerString());
            // タイトルを設定
            stage.setTitle("各種タイマー画面");
            // 大きさを設定
            stage.setScene(new Scene(root, 310, 160));
            // 最小の大きさを設定
            stage.setMinWidth(330);
            stage.setMinHeight(210);
            // 最前面設定
            stage.setAlwaysOnTop(true);
            // ×ボタンを押した際の設定
            stage.setOnCloseRequest(req -> {
                OpenTimerFlg.set(false);
                this.setImage = null;
                this.setText = null;
            });
            // 新しいウインドウを表示
            stage.show();
            OpenTimerFlg.set(true);
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showDialog("各種タイマー画面を開けませんでした。", "IOエラー", Alert.AlertType.ERROR);
        }
    }
    /**
     * 画像認識支援画面を開く
     */
    public void openSceneHelperCommand(){
        try {
            // 新しいウインドウを生成
            final var stage = new Stage();
            // ウィンドウの中身をFXMLから読み込み
            final var loader = new FXMLLoader(
                    ClassLoader.getSystemResource("com/ysrken/kamo/View/SceneHelperView.fxml"));
            final Parent root = loader.load();
            final SceneHelperController controller = loader.getController();
            // タイトルを設定
            stage.setTitle("画像認識支援画面");
            // 大きさを設定
            stage.setScene(new Scene(root, 500, 400));
            // 最小の大きさを設定
            stage.setMinWidth(400);
            stage.setMinHeight(300);
            // 最前面設定
            stage.setAlwaysOnTop(true);
            // ×ボタンを押した際の設定
            stage.setOnCloseRequest(req -> {
                OpenSceneHelperFlg.set(false);
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
                            controller.setImage(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
            });
            // 新しいウインドウを表示
            stage.show();
            OpenSceneHelperFlg.set(true);
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showDialog("画像認識支援画面を開けませんでした。", "IOエラー", Alert.AlertType.ERROR);
        }
    }
    /**
     * オンラインヘルプ(Wiki)を開く
     */
    public void openWikiCommand(){
        final var desktop = Desktop.getDesktop();
        try{
            desktop.browse(new URI(Utility.HELP_URL));
        }catch(Exception e){
            e.printStackTrace();
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
