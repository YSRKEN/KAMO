package com.ysrken.kamo.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.service.SettingService;
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
	
    /**
     * コンストラクタ
     */
    public MainModel() {
    	// 設定変更時に情報を記録する
    	openBattleSceneReflectionFlg.addListener((ob, o, n) -> setting.setSetting("OpenBattleSceneReflectionFlg", n));
    	openTimerFlg.addListener((ob, o, n) -> setting.setSetting("OpenTimerFlg", n));
    	openSceneHelperFlg.addListener((ob, o, n) -> setting.setSetting("OpenSceneHelperFlg", n));
    	autoGetPositionFlg.addListener((ob, o, n) -> setting.setSetting("AutoGetPositionFlg", n));
    	blindNameTextFlg.addListener((ob, o, n) -> setting.setSetting("BlindNameTextFlg", n));
    	specialGetPosFlg.addListener((ob, o, n) -> setting.setSetting("SpecialGetPosFlg", n));
    	saveWindowPositionFlg.addListener((ob, o, n) -> setting.setSetting("SaveWindowPositionFlg", n));
    }
    
    /**
     * 初期化
     */
    public void initialize(){
    	openBattleSceneReflectionFlg.set(setting.getSetting("OpenBattleSceneReflectionFlg"));
    	openTimerFlg.set(setting.getSetting("OpenTimerFlg"));
    	openSceneHelperFlg.set(setting.getSetting("OpenSceneHelperFlg"));
    	autoGetPositionFlg.set(setting.getSetting("AutoGetPositionFlg"));
    	blindNameTextFlg.set(setting.getSetting("BlindNameTextFlg"));
    	specialGetPosFlg.set(setting.getSetting("SpecialGetPosFlg"));
    	saveWindowPositionFlg.set(setting.getSetting("SaveWindowPositionFlg"));
    	/*if(openBattleSceneReflectionFlg.get()) {
    		openBattleSceneReflectionCommand();
    	}
    	if(openTimerFlg.get()) {
    		openTimerCommand();
    	}
    	if(openSceneHelperFlg.get()) {
    		openSceneHelperCommand();
    	}*/
    }
    
	/**
     * 終了コマンド
     */
	public void exitCommand() {
		Platform.exit();
	}
	
	/**
	 * 座標取得コマンド
	 */
	public void getPositionCommand() {
		// スタブ
		disableSaveScreenshotFlg.set(false);
	}
	
	/**
	 * 戦闘振り返り画面を開くコマンド
	 */
	public void openBattleSceneReflectionCommand() {
		if(battleSceneReflectionStage == null) {
			battleSceneReflectionStage = factory.create("/fxml/hello.fxml", "BattleSceneReflectionWindow");
			battleSceneReflectionStage.setTitle("戦闘振り返り画面");
			battleSceneReflectionStage.setWidth(400);
			battleSceneReflectionStage.setHeight(300);
			openBattleSceneReflectionFlg.set(true);
			battleSceneReflectionStage.setOnCloseRequest(() -> {
				battleSceneReflectionStage = null;
				openBattleSceneReflectionFlg.set(false);
    		});
			battleSceneReflectionStage.show();
    	}
	}
	
	/**
	 * 各種タイマー画面を開くコマンド
	 */
	public void openTimerCommand() {
		if(timerStage == null) {
			timerStage = factory.create("/fxml/hello.fxml", "TimerWindow");
			timerStage.setTitle("各種タイマー画面");
			timerStage.setWidth(400);
			timerStage.setHeight(300);
			openTimerFlg.set(true);
			timerStage.setOnCloseRequest(() -> {
				timerStage = null;
				openTimerFlg.set(false);
    		});
			timerStage.show();
    	}
	}
	
	/**
	 * 画像認識支援画面を開くコマンド
	 */
	public void openSceneHelperCommand() {
		if(sceneHelperStage == null) {
			sceneHelperStage = factory.create("/fxml/hello.fxml", "SceneHelperWindow");
			sceneHelperStage.setTitle("画像認識支援画面");
			sceneHelperStage.setWidth(400);
			sceneHelperStage.setHeight(300);
			openSceneHelperFlg.set(true);
			sceneHelperStage.setOnCloseRequest(() -> {
				sceneHelperStage = null;
				openSceneHelperFlg.set(false);
    		});
			sceneHelperStage.show();
    	}
	}
}
