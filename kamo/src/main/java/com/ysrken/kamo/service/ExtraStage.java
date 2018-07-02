package com.ysrken.kamo.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExtraStage {
	private Stage stage;
	
	@Autowired
	private LoggerService logger;
	
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
        
        // ウィンドウが移動・リサイズした際のイベントを登録する
        stage.xProperty().addListener((ob, o, n) -> showWindowRect());
        stage.yProperty().addListener((ob, o, n) -> showWindowRect());
        stage.widthProperty().addListener((ob, o, n) -> showWindowRect());
        stage.heightProperty().addListener((ob, o, n) -> showWindowRect());
	}
	
	private void showWindowRect() {
		//logger.debug("Rect→(" + stage.getX() + "," + stage.getY() + ")-" + stage.getWidth() + "x" + stage.getHeight());
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
