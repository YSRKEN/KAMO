package com.ysrken.kamo.stage;

import java.io.IOException;

import javafx.stage.Stage;

/**
 * Stageを拡張したクラス用のインターフェース
 * @author ysrken
 */
public interface ExtraStage {

	/**
	 * 初期化する
	 */
	void initialize(Stage stage, String fxmlPath, String keyWord) throws IOException;
	
	/**
	 * ウィンドウを表示する
	 */
	void show();

	/**
	 * タイトルを設定する
	 * @param title
	 */
	void setTitle(String title);

	/**
	 * ウィンドウの×ボタンを押した際の動きを指定する
	 * @param func
	 */
	void setOnCloseRequest(Runnable func);
}