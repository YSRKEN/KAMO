package com.ysrken.kamo.stage;

import java.awt.Rectangle;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;
import com.ysrken.kamo.service.LoggerService;
import com.ysrken.kamo.service.SettingService;

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
public class ExtraStageImpl implements ExtraStage {
	private Stage stage;
	
	@Autowired
	private LoggerService logger;
	
    @Autowired
    private SettingService settingService;
	
	/**
	 * コンストラクタ
	 * @param stage 食わせるStage型のインスタンス
	 * @param fxmlPath FXMLファイルへのパス
	 * @throws IOException 
	 */
	public ExtraStageImpl(Stage stage, String fxmlPath) throws IOException {
		// Stage情報を記録する
		this.stage = stage;
		
		// FXMLを読み込み、Stageに設定する
		FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
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
		settingService.getSetting().setMainWindowRectX((int)stage.getX());
		settingService.getSetting().setMainWindowRectY((int)stage.getY());
		settingService.getSetting().setMainWindowRectW((int)stage.getWidth());
		settingService.getSetting().setMainWindowRectH((int)stage.getHeight());
		settingService.saveSetting();
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#show()
	 */
	@Override
	public void show() {
		stage.show();
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		stage.setTitle(title);
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setWidth(double)
	 */
	@Override
	public void setWidth(double width) {
		this.stage.setWidth(width);
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setHeight(double)
	 */
	@Override
	public void setHeight(double height) {
		this.stage.setHeight(height);
	}
}
