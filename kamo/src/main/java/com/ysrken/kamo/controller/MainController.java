package com.ysrken.kamo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.model.MainModel;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
	@FXML private MenuItem AutoGetPositionMenu;
	@FXML private MenuItem BlindNameTextMenu;
	@FXML private MenuItem SpecialGetPosMenu;
	@FXML private MenuItem SaveWindowPositionMenu;
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
	MainModel mainModel;
	
	/**
	 * 初期化
	 */
	public void initialize(){
		// メソッドをコントロールに割り当てる
		ExitMenu.setOnAction(EventHandler -> mainModel.exitCommand());
	}
}
