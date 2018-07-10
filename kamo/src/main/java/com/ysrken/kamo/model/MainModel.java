package com.ysrken.kamo.model;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ysrken.kamo.Constant;
import com.ysrken.kamo.service.PictureProcessingService;
import com.ysrken.kamo.service.ScreenshotService;
import com.ysrken.kamo.service.SettingService;
import com.ysrken.kamo.service.UtilityService;
import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert.AlertType;
import lombok.Getter;

/**
 * メイン画面のModel
 * @author ysrken
 */
@Component
public class MainModel {
	/**
	 * スクリーンショットボタンを押せないか？
	 */
	@Getter
    private BooleanProperty disableSaveScreenshotFlg = new SimpleBooleanProperty(true);
	
	/**
	 * 各種画面を開いているか？
	 */
	@Getter
	private BooleanProperty openBattleSceneReflectionFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty openTimerFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty openSceneHelperFlg = new SimpleBooleanProperty(false);
	
	/**
	 * 各種設定項目
	 */
	@Getter
	private BooleanProperty autoGetPositionFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty blindNameTextFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty specialGetPosFlg = new SimpleBooleanProperty(false);
	@Getter
	private BooleanProperty saveWindowPositionFlg = new SimpleBooleanProperty(false);
	
	/**
	 * シーン情報
	 */
	@Getter
	private StringProperty nowSceneText = new SimpleStringProperty("シーン判定：[不明]");
	
	/**
	 * 動作ログ情報
	 */
	@Getter
	private StringProperty messageLogText = new SimpleStringProperty("");
	
	
	/**
	 * 各種画面
	 */
	private ExtraStage battleSceneReflectionStage = null;
	private ExtraStage timerStage = null;
	private ExtraStage sceneHelperStage = null;
	
	/**
	 * 各種サービス
	 */
    @Autowired
    private ExtraStageFactory factory;
    @Autowired
    private SettingService setting;
    @Autowired
    private UtilityService utility;
    @Autowired
    private ScreenshotService screenshot;
    @Autowired
    private PictureProcessingService pictureProcessing;
	
    /**
     * ログにテキストを追加
     * @param text
     */
    private void addLogText(String text){
        Platform.runLater(() -> {
        	messageLogText.set(String.format(
                    "%s%s %s%n",
                    messageLogText.get(),
                    utility.getDateStringShort(),
                    text
            ));
        });
    }
    
    /**
     * 長い周期で行われるタスクを設定
     */
    private class LongIntervalTask extends TimerTask{
        public void run(){
            // スクリーンショットが撮影可能な場合の処理
            if(screenshot != null && screenshot.canGetScreenshot()){
                // ゲーム画面の位置が移動した際の処理
                if(screenshot.isMovedPosition()){
                    addLogText("【位置ズレ検知】");
                    addLogText("自動で再取得を試みます...");
                    getPositionCommand();
                }
            }else if(setting != null && setting.<Boolean>getSetting("AutoGetPositionFlg")){
                addLogText("【自動座標認識】");
                addLogText("自動で再取得を試みます...");
                getPositionCommand();
            }
        }
    }
    
    /**
     * コンストラクタ
     */
    public MainModel() {
    	System.out.println("DEBUG MainApp - MainModel#MainModel");
    	// 設定変更時に情報を記録する
    	autoGetPositionFlg.addListener((ob, o, n) -> setting.setSetting("AutoGetPositionFlg", n));
    	blindNameTextFlg.addListener((ob, o, n) -> setting.setSetting("BlindNameTextFlg", n));
    	specialGetPosFlg.addListener((ob, o, n) -> setting.setSetting("SpecialGetPosFlg", n));
    	saveWindowPositionFlg.addListener((ob, o, n) -> setting.setSetting("SaveWindowPositionFlg", n));
    	
        // 長周期で実行されるタイマー
        final Timer longIntervalTimer = new Timer();
        longIntervalTimer.schedule(new LongIntervalTask(), 0, 1000);
    }
    
    /**
     * 初期化
     * @throws IOException 
     * @throws JsonProcessingException 
     */
    public void initialize() throws JsonProcessingException, IOException{
    	System.out.println("DEBUG MainApp - MainModel#initialize");
    	// 設定を読み込んだ上で画面に反映する
    	autoGetPositionFlg.set(setting.getSetting("AutoGetPositionFlg"));
    	blindNameTextFlg.set(setting.getSetting("BlindNameTextFlg"));
    	specialGetPosFlg.set(setting.getSetting("SpecialGetPosFlg"));
    	saveWindowPositionFlg.set(setting.getSetting("SaveWindowPositionFlg"));
    	
    	// Beanの初期化
    	pictureProcessing.initialize();
    }
    
	/**
     * 終了コマンド
     */
	public void exitCommand() {
		Platform.exit();
		System.exit(0);
	}
	
	/**
	 * 座標取得コマンド
	 */
	public void getPositionCommand() {
		addLogText("【座標取得】");
		
		// 取得操作を行う
        final boolean getPositionFlg = screenshot.trySearchGamePosition();

        // 取得に成功したか否かで処理を分ける
        if(getPositionFlg){
            // ゲーム座標を取得する
            final Rectangle rect = screenshot.getPosition();

            // 取得したゲーム座標を記録する
            addLogText("座標取得：OK");
            addLogText(String.format("取得位置：(%d,%d)-%dx%d",
                    rect.x, rect.y, rect.width, rect.height));
            
            // スクリーンショットを使用可能にする
            disableSaveScreenshotFlg.set(false);
        }else{
            addLogText("座標取得：NG");
            // スクリーンショットを使用不可にする
            disableSaveScreenshotFlg.set(true);
        }
	}
	
	/**
	 * スクショコマンド
	 */
	public void saveScreenshotCommand() {
		addLogText("【スクリーンショット】");
        if(screenshot.canGetScreenshot()){
            final BufferedImage screenShot = screenshot.getScreenshot();
            final BufferedImage processedImage = pictureProcessing.getProcessedImage(screenShot);
            final String fileName = String.format("%s.png", utility.getDateStringLong());
            try {
                ImageIO.write(processedImage, "png", new File(String.format("pic\\%s", fileName)));
                addLogText(String.format("ファイル名：%s", fileName));
            } catch (IOException e) {
                e.printStackTrace();
                addLogText("エラー：スクリーンショットの保存に失敗しました。");
            }
        }else{
            addLogText("エラー：スクリーンショットを取得できません。");
        }
	}
	
    /**
     * スクショの保存先であるpicフォルダを開く
     * パス指定の時点で明らかなように、Windowsにしか対応していない
     */
    public void openPicFolderCommand(){
        try {
            final Runtime rt = Runtime.getRuntime();
            String cmd = String.format("explorer %s\\pic", System.getProperty("user.dir"));
            rt.exec(cmd);
        } catch (IOException e) {
            utility.showDialog("picフォルダを開けませんでした。", "IOエラー", AlertType.ERROR);
        }
    }
	
	/**
	 * 戦闘振り返り画面を開くコマンド
	 */
	public void openBattleSceneReflectionCommand() {
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(battleSceneReflectionStage != null) {
			return;
		}
		
		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：戦闘振り返り画面");
		
		// ウィンドウのStageを作成する
		try {
			battleSceneReflectionStage = factory.create("/fxml/hello.fxml", "BattleSceneReflectionWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
			return;
		}

		// タイトルを設定する
		battleSceneReflectionStage.setTitle("戦闘振り返り画面");
		
		// 既にウィンドウを表示した、というフラグを立てる
		openBattleSceneReflectionFlg.set(true);

		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		battleSceneReflectionStage.setOnCloseRequest(() -> {
			battleSceneReflectionStage = null;
			openBattleSceneReflectionFlg.set(false);
		});
		
		// ウィンドウを表示する
		battleSceneReflectionStage.show();
	}
	
	/**
	 * 各種タイマー画面を開くコマンド
	 */
	public void openTimerCommand() {
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(timerStage != null) {
			return;
		}
		
		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：各種タイマー画面");
		
		// ウィンドウのStageを作成する
		try {
			timerStage = factory.create("/fxml/hello.fxml", "TimerWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
		}
		
		// タイトルを設定する
		timerStage.setTitle("各種タイマー画面");
		
		// 既にウィンドウを表示した、というフラグを立てる
		openTimerFlg.set(true);
		
		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		timerStage.setOnCloseRequest(() -> {
			timerStage = null;
			openTimerFlg.set(false);
		});
		
		// ウィンドウを表示する
		timerStage.show();
	}
	
	/**
	 * 画像認識支援画面を開くコマンド
	 */
	public void openSceneHelperCommand() {
		// nullでない＝既にそのウィンドウが開いている＝これ以上開く必要はない
		if(sceneHelperStage != null) {
			return;
		}

		// 動作ログに残す
		addLogText("【ウィンドウ】");
		addLogText("名称：画像認識支援画面");
		
		// ウィンドウのStageを作成する
		try {
			sceneHelperStage = factory.create("/fxml/hello.fxml", "SceneHelperWindow");
		} catch (IOException e) {
			addLogText("エラー：IOエラーが発生しました。");
			e.printStackTrace();
		}
		
		// タイトルを設定する
		sceneHelperStage.setTitle("画像認識支援画面");
		
		// 既にウィンドウを表示した、というフラグを立てる
		openSceneHelperFlg.set(true);
		
		// ウィンドウが閉じられた際の処理を記述する
		// (再度ウィンドウを開けるようにリセット)
		sceneHelperStage.setOnCloseRequest(() -> {
			sceneHelperStage = null;
			openSceneHelperFlg.set(false);
		});
		
		// ウィンドウを表示する
		sceneHelperStage.show();
	}
	
    /**
     * ソフトウェアの更新が来ているかをチェックする
     */
    public void checkVersionCommand(){
        try {
            addLogText("【更新チェック】");
            // 更新情報を表すテキストファイルをダウンロードする
            final String checkText = utility.downloadTextData("https://raw.githubusercontent.com/YSRKEN/KAMO/master/version.txt");
            if(checkText.isEmpty())
                throw new IOException();
            // 更新文字列は「1,1.0.0」のような書式になっているはずなので確認する
            final String[] temp = checkText.split(",");
            if(temp.length < 2){
                throw new NumberFormatException();
            }
            // 情報を読み取っていく
            final int revision = Integer.parseInt(temp[0]);
            addLogText(String.format("現在のバージョン：%s, リビジョン：%d",
                    Constant.SOFTWARE_VER, Constant.SOFTWARE_REVISION));
            addLogText(String.format("最新のバージョン：%s, リビジョン：%d",
                    temp[1], revision));
            if(Constant.SOFTWARE_REVISION < revision){
                String message = String.format(
                        "より新しいバージョンが見つかりました。%n現在のバージョン：%s%n最新のバージョン：%s%nダウンロードサイトを開きますか？",
                        Constant.SOFTWARE_VER, temp[1]
                );
                final boolean openUrlFlg = utility.showChoiceDialog(message, "更新チェック");
                if(openUrlFlg){
                    final Desktop desktop = Desktop.getDesktop();
                    try{
                        desktop.browse(new URI(Constant.SOFTWARE_URL));
                    }catch( Exception e ){
                        e.printStackTrace();
                    }
                }
            }else{
                addLogText("このソフトウェアは最新です。");
            }
        }catch(NumberFormatException | IOException e){
            e.printStackTrace();
            addLogText("エラー：更新データを確認できませんでした。");
        }
    }
	
    /**
     * オンラインヘルプ(Wiki)を開く
     */
    public void openWikiCommand(){
        final Desktop desktop = Desktop.getDesktop();
        try{
            desktop.browse(new URI(Constant.HELP_URL));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * バージョン情報を表示する
     */
    public void aboutCommand(){
        final String contentText = String.format("ソフト名：%s%nバージョン：%s%n作者：%s",
        		Constant.SOFTWARE_NAME,
        		Constant.SOFTWARE_VER,
        		Constant.SOFTWARE_AUTHOR
        );
        utility.showDialog(contentText, "バージョン情報");
    }
}
