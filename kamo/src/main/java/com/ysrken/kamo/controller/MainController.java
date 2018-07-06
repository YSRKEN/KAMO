package com.ysrken.kamo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.model.MainModel;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

/**
 * メイン画面のController
 * @author ysrken
 */
@Component
public class MainController {
	/**
	 * 「終了」メニュー
	 */
	@FXML private MenuItem ExitMenu;
	
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
