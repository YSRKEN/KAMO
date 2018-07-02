package com.ysrken.kamo.control;

import java.io.IOException;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ysrken.kamo.MainApp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Stageを拡張したクラス
 * @author ysrken
 *
 */
public class ExtraStage {
	private Stage stage;
	
	/**
	 * コンストラクタ
	 * @param stage 食わせるStage型のインスタンス
	 * @param fxmlPath FXMLファイルへのパス
	 * @throws IOException 
	 */
	public ExtraStage(Stage stage, String fxmlPath) throws IOException {
		// Stage情報を記録する
		this.stage = stage;
		
		// FXMLを読み込み、Stageに設定する
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(MainApp.class);
		FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(context::getBean);
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlPath));
        Scene scene = new Scene(rootNode);
        scene.getStylesheets().add("/styles/styles.css");
        this.stage.setScene(scene);
	}
	
	/**
	 * ウィンドウを表示する
	 */
	public void show() {
		stage.show();
	}
	
	/**
	 * タイトルを設定する
	 * @param title
	 */
	public void setTitle(String title) {
		stage.setTitle(title);
	}
	
	/**
	 * 横幅を設定する
	 * @param width
	 */
	public void setWidth(double width) {
		this.stage.setWidth(width);
	}
	
	/**
	 * 縦幅を設定する
	 * @param height
	 */
	public void setHeight(double height) {
		this.stage.setHeight(height);
	}
}
