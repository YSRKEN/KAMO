package com.ysrken.kamo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.model.MainModel;
import com.ysrken.kamo.service.UtilityService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;

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
	@FXML private CheckMenuItem BlindNameTextMenu;
	@FXML private CheckMenuItem SpecialGetPosMenu;
	@FXML private CheckMenuItem SaveWindowPositionMenu;
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
	 */
	public void initialize(){		
		// メソッドをコントロールに割り当てる
		ExitMenu.setOnAction(e -> model.exitCommand());
		GetPositionMenu.setOnAction(e -> model.getPositionCommand());
		GetPositionButton.setOnAction(e -> model.getPositionCommand());
		OpenBattleSceneReflectionMenu.setOnAction(e -> model.openBattleSceneReflectionCommand());
		OpenTimerMenu.setOnAction(e -> model.openTimerCommand());
		OpenSceneHelperMenu.setOnAction(e -> model.openSceneHelperCommand());
		
		// プロパティをData Bindingさせる
		SaveScreenshotMenu.disableProperty().bind(model.getDisableSaveScreenshotFlg());
		SaveScreenshotButton.disableProperty().bind(model.getDisableSaveScreenshotFlg());
		OpenBattleSceneReflectionMenu.disableProperty().bind(model.getOpenBattleSceneReflectionFlg());
		OpenTimerMenu.disableProperty().bind(model.getOpenTimerFlg());
		OpenSceneHelperMenu.disableProperty().bind(model.getOpenSceneHelperFlg());
		NowSceneTextLabel.textProperty().bind(model.getNowSceneText());
		AutoGetPositionMenu.selectedProperty().bindBidirectional(model.getAutoGetPositionFlg());
		BlindNameTextMenu.selectedProperty().bindBidirectional(model.getBlindNameTextFlg());
		SpecialGetPosMenu.selectedProperty().bindBidirectional(model.getSpecialGetPosFlg());
		SaveWindowPositionMenu.selectedProperty().bindBidirectional(model.getSaveWindowPositionFlg());
		
		// 使えない設定をdisableする
        OpenPicFolderMenu.setDisable(!utility.isWindows());
        SpecialGetPosMenu.setDisable(!utility.isWindows());
        
		// Modelの初期化
		model.initialize();
	}
}
