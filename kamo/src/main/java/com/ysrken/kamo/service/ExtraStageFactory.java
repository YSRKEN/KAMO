package com.ysrken.kamo.service;

import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;

import javafx.stage.Stage;

/**
 * ExtraStageクラスのファクトリクラス
 * @author ysrken
 */
@Component
public class ExtraStageFactory {
	
	/**
	 * ExtraStageクラスのインスタンスを生成
	 * @param stage 元となるStage
	 * @param fxmlPath FXMLファイルのパス
	 * @return ExtraStageクラスのインスタンス
	 */
	public ExtraStageImpl create(Stage stage, String fxmlPath) {
		return MainApp.getApplicationContext().getBean(ExtraStage.class, stage, fxmlPath);
	}
	
	/**
	 * ExtraStageクラスのインスタンスを生成
	 * @param fxmlPath FXMLファイルのパス
	 * @return ExtraStageクラスのインスタンス
	 */
	public ExtraStageImpl create(String fxmlPath) {
		return create(new Stage(), fxmlPath);
	}
}
