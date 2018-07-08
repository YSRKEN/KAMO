package com.ysrken.kamo.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.service.SettingService;
import com.ysrken.kamo.service.UtilityService;
import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
     * コンストラクタ
     */
    public MainModel() {
    	// 設定変更時に情報を記録する
    	autoGetPositionFlg.addListener((ob, o, n) -> setting.setSetting("AutoGetPositionFlg", n));
    	blindNameTextFlg.addListener((ob, o, n) -> setting.setSetting("BlindNameTextFlg", n));
    	specialGetPosFlg.addListener((ob, o, n) -> setting.setSetting("SpecialGetPosFlg", n));
    	saveWindowPositionFlg.addListener((ob, o, n) -> setting.setSetting("SaveWindowPositionFlg", n));
    }
    
    /**
     * 初期化
     */
    public void initialize(){
    	// 設定を読み込んだ上で画面に反映する
    	autoGetPositionFlg.set(setting.getSetting("AutoGetPositionFlg"));
    	blindNameTextFlg.set(setting.getSetting("BlindNameTextFlg"));
    	specialGetPosFlg.set(setting.getSetting("SpecialGetPosFlg"));
    	saveWindowPositionFlg.set(setting.getSetting("SaveWindowPositionFlg"));
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
		// スタブ
		disableSaveScreenshotFlg.set(false);
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
		battleSceneReflectionStage = factory.create("/fxml/hello.fxml", "BattleSceneReflectionWindow");

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
		timerStage = factory.create("/fxml/hello.fxml", "TimerWindow");
		
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
		sceneHelperStage = factory.create("/fxml/hello.fxml", "SceneHelperWindow");
		
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
}
