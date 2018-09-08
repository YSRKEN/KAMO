package com.ysrken.kamo.stage;

import java.io.IOException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

	/**
	 * ファイルがドラッグされてきた際に転送モードを変更する
	 */
	void setOnDragOver();

	/**
	 * ファイルがドラッグされてきた際の処理を設定する
	 */
	<File> void setOnDragDropped(Consumer<File> func);
}