package com.ysrken.kamo.stage;

import java.io.IOException;

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
	 * @throws IOException 
	 */
	public ExtraStage create(Stage stage, String fxmlPath, String keyWord) throws IOException {
		ExtraStage extraStage = MainApp.getApplicationContext().getBean(ExtraStageImpl.class);
		extraStage.initialize(stage, fxmlPath, keyWord);
		return extraStage;
	}
	
	/**
	 * ExtraStageクラスのインスタンスを生成
	 * @param fxmlPath FXMLファイルのパス
	 * @return ExtraStageクラスのインスタンス
	 * @throws IOException 
	 */
	public ExtraStage create(String fxmlPath, String keyWord) throws IOException {
		return create(new Stage(), fxmlPath, keyWord);
	}
}
