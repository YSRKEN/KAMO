package com.ysrken.kamo.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * ExtraStageImplの実装クラス
 * @author ysrken
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExtraStage implements ExtraStageImpl {
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
	
	/**
	 * ウィンドウのRectをロギング
	 */
	private void showWindowRect() {
		logger.debug("Rect→(" + stage.getX() + "," + stage.getY() + ")-" + stage.getWidth() + "x" + stage.getHeight());
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStageImpl#show()
	 */
	@Override
	public void show() {
		stage.show();
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStageImpl#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		stage.setTitle(title);
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStageImpl#setWidth(double)
	 */
	@Override
	public void setWidth(double width) {
		this.stage.setWidth(width);
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStageImpl#setHeight(double)
	 */
	@Override
	public void setHeight(double height) {
		this.stage.setHeight(height);
	}
}
