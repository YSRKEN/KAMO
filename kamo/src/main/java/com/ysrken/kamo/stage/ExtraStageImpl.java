package com.ysrken.kamo.stage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ysrken.kamo.MainApp;
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
	
	/**
	 * Stage情報
	 */
	private Stage stage;
	
	/**
	 * ウィンドウを一意に特定するためのキー
	 */
	private String keyWord;
	
    @Autowired
    private SettingService setting;
	
	/**
	 * コンストラクタ
	 * @param stage 食わせるStage型のインスタンス
	 * @param fxmlPath FXMLファイルへのパス
	 * @throws IOException 
	 */
	public ExtraStageImpl(Stage stage, String fxmlPath, String keyWord) throws IOException {
		// Stage情報を記録する
		this.stage = stage;
		// キー情報を記録する
		this.keyWord = keyWord;
		
		// FXMLを読み込み、Stageに設定する
		FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlPath));
        Scene scene = new Scene(rootNode);
        scene.getStylesheets().add("/styles/styles.css");
        this.stage.setScene(scene);
        
        // その他の情報を登録する
        this.stage.setAlwaysOnTop(true);
        
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
		setting.setSetting(keyWord, new double[] {
				stage.getX(),
				stage.getY(),
				stage.getWidth(),
				stage.getHeight()
		});
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
		this.stage.setMinWidth(width);
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setHeight(double)
	 */
	@Override
	public void setHeight(double height) {
		this.stage.setHeight(height);
		this.stage.setMinHeight(height);
	}
	
	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setOnCloseRequest(Runnable)
	 */
	@Override
	public void setOnCloseRequest(Runnable func) {
		this.stage.setOnCloseRequest(req -> func.run());
	}
}
