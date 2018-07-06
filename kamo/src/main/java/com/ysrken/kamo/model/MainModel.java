package com.ysrken.kamo.model;

import org.springframework.stereotype.Component;

import javafx.application.Platform;

/**
 * メイン画面のModel
 * @author ysrken
 */
@Component
public class MainModel {
	/**
     * 終了コマンド
     */
	public void exitCommand() {
		Platform.exit();
	}
}
