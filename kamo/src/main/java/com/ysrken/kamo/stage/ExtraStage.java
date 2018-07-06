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
	 * 横幅を設定する
	 * @param width
	 */
	void setWidth(double width);

	/**
	 * 縦幅を設定する
	 * @param height
	 */
	void setHeight(double height);

	/**
	 * ウィンドウの×ボタンを押した際の動きを指定する
	 * @param func
	 */
	void setOnCloseRequest(Runnable func);
}