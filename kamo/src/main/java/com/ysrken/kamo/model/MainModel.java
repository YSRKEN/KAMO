package com.ysrken.kamo.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
