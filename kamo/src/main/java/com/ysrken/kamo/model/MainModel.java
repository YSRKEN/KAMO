package com.ysrken.kamo.model;

import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
}
