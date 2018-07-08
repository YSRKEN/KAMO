package com.ysrken.kamo.stage;

/**
 * Stageを拡張したクラス用のインターフェース
 * @author ysrken
 */
public interface ExtraStage {

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