package com.ysrken.kamo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ysrken.kamo.model.MainModel;
import com.ysrken.kamo.service.UtilityService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * メイン画面のController
 * @author ysrken
 */
@Component
public class MainController {
	/**
	 * 各種メニュー
	 */
	@FXML private MenuItem ExitMenu;

	@FXML private MenuItem GetPositionMenu;
	@FXML private MenuItem SaveScreenshotMenu;
	@FXML private MenuItem OpenPicFolderMenu;

	@FXML private MenuItem OpenBattleSceneReflectionMenu;
	@FXML private MenuItem OpenTimerMenu;
	@FXML private MenuItem OpenSceneHelperMenu;

	@FXML private CheckMenuItem AutoGetPositionMenu;
	@FXML private CheckMenuItem DisableCheckMovedPositionMenu;
	@FXML private CheckMenuItem BlindNameTextMenu;
	@FXML private CheckMenuItem SpecialGetPosMenu;
	@FXML private CheckMenuItem SaveWindowPositionMenu;

	@FXML private RadioMenuItem UpdateFps01Menu;
	@FXML private RadioMenuItem UpdateFps03Menu;
	@FXML private RadioMenuItem UpdateFps05Menu;
	@FXML private RadioMenuItem UpdateFps10Menu;
	@FXML private RadioMenuItem UpdateFps15Menu;
	@FXML private RadioMenuItem UpdateFps30Menu;
	@FXML private RadioMenuItem UpdateFps60Menu;


	@FXML private MenuItem OpenWikiMenu;
	@FXML private MenuItem CheckVersionMenu;
	@FXML private MenuItem AboutMenu;
	
	/**
	 * その他コントロール
	 */
	@FXML private Button GetPositionButton;
	@FXML private Button SaveScreenshotButton;
	@FXML private Label NowSceneTextLabel;
	@FXML private TextArea MessageLogTextArea;
	
	/**
	 * Model情報
	 */
	@Autowired
	MainModel model;
	
	/**
	 * 各種サービス
	 */
	@Autowired
	UtilityService utility;
	
	/**
	 * 初期化
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
	public void initialize() throws JsonProcessingException, IOException{
		System.out.println("DEBUG MainApp - MainController#initialize");
		
		// メソッドをコントロールに割り当てる
		ExitMenu.setOnAction(e -> model.exitCommand());

		GetPositionMenu.setOnAction(e -> model.getPositionCommand());
		GetPositionButton.setOnAction(e -> model.getPositionCommand());
		SaveScreenshotMenu.setOnAction(e -> model.saveScreenshotCommand());
		SaveScreenshotButton.setOnAction(e -> model.saveScreenshotCommand());
		OpenPicFolderMenu.setOnAction(e -> model.openPicFolderCommand());

		OpenBattleSceneReflectionMenu.setOnAction(e -> model.openBattleSceneReflectionCommand());
		OpenTimerMenu.setOnAction(e -> model.openTimerCommand());
		OpenSceneHelperMenu.setOnAction(e -> model.openSceneHelperCommand());

		OpenWikiMenu.setOnAction(e -> model.openWikiCommand());
		CheckVersionMenu.setOnAction(e -> model.checkVersionCommand());
		AboutMenu.setOnAction(e -> model.aboutCommand());
		
		// プロパティをData Bindingさせる
		SaveScreenshotMenu.disableProperty().bind(model.getDisableSaveScreenshotFlg());
		SaveScreenshotButton.disableProperty().bind(model.getDisableSaveScreenshotFlg());

		OpenBattleSceneReflectionMenu.disableProperty().bind(model.getOpenBattleSceneReflectionFlg());
		OpenTimerMenu.disableProperty().bind(model.getOpenTimerFlg());
		OpenSceneHelperMenu.disableProperty().bind(model.getOpenSceneHelperFlg());

		AutoGetPositionMenu.selectedProperty().bindBidirectional(model.getAutoGetPositionFlg());
		DisableCheckMovedPositionMenu.selectedProperty().bindBidirectional(model.getDisableCheckMovedPositionFlg());
		BlindNameTextMenu.selectedProperty().bindBidirectional(model.getBlindNameTextFlg());
		SpecialGetPosMenu.selectedProperty().bindBidirectional(model.getSpecialGetPosFlg());
		SaveWindowPositionMenu.selectedProperty().bindBidirectional(model.getSaveWindowPositionFlg());
		UpdateFps01Menu.selectedProperty().bindBidirectional(model.getUpdateFps01Flg());
		UpdateFps03Menu.selectedProperty().bindBidirectional(model.getUpdateFps03Flg());
		UpdateFps05Menu.selectedProperty().bindBidirectional(model.getUpdateFps05Flg());
		UpdateFps10Menu.selectedProperty().bindBidirectional(model.getUpdateFps10Flg());
		UpdateFps15Menu.selectedProperty().bindBidirectional(model.getUpdateFps15Flg());
		UpdateFps30Menu.selectedProperty().bindBidirectional(model.getUpdateFps30Flg());
		UpdateFps60Menu.selectedProperty().bindBidirectional(model.getUpdateFps60Flg());

		NowSceneTextLabel.textProperty().bind(model.getNowSceneText());
		
		// 使えない設定をdisableする
        OpenPicFolderMenu.setDisable(!utility.isWindows());
        SpecialGetPosMenu.setDisable(!utility.isWindows());
        
        // 内容が変更された際の動作を記述する
        model.getMessageLogText().addListener((ob, o, n) -> {
        	MessageLogTextArea.setText(n);
        	MessageLogTextArea.setScrollTop(Double.POSITIVE_INFINITY);
        });
        
		// Modelの初期化
		model.initialize();
		
		// ソフトウェアの更新を確認する
		model.checkVersionCommand();
	}
}
