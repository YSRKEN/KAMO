package com.ysrken.kamo.stage;

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
	public ExtraStage create(Stage stage, String fxmlPath, String keyWord) {
		return MainApp.getApplicationContext().getBean(ExtraStageImpl.class, stage, fxmlPath, keyWord);
	}
	
	/**
	 * ExtraStageクラスのインスタンスを生成
	 * @param fxmlPath FXMLファイルのパス
	 * @return ExtraStageクラスのインスタンス
	 */
	public ExtraStage create(String fxmlPath, String keyWord) {
		return create(new Stage(), fxmlPath, keyWord);
	}
}
