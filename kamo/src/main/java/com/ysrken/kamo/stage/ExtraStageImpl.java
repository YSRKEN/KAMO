package com.ysrken.kamo.stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
	
    public ExtraStageImpl() {
    	System.out.println("DEBUG MainApp - ExtraStageImpl#ExtraStageImpl");
    }
    
	/**
	 * 初期化する
	 * @param stage 食わせるStage型のインスタンス
	 * @param fxmlPath FXMLファイルへのパス
	 * @throws IOException 
	 */
	public void initialize(Stage stage, String fxmlPath, String keyWord) throws IOException {
		System.out.println("DEBUG MainApp - ExtraStageImpl#initialize");
		
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
        
        // ウィンドウの座標を指定する
        ArrayList<Double> rect = setting.<Boolean>getSetting("SaveWindowPositionFlg")
        		? setting.<ArrayList<Double>>getSetting(keyWord)
        		: setting.<ArrayList<Double>>getDefaultSetting(keyWord);
        stage.setX(rect.get(0));
        stage.setY(rect.get(1));
        stage.setWidth(rect.get(2));
        stage.setHeight(rect.get(3));
        
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
		setting.setSetting(keyWord, new ArrayList<Double>(Arrays.asList(
				stage.getX(),
				stage.getY(),
				stage.getWidth(),
				stage.getHeight()
		)));
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
	 * @see com.ysrken.kamo.service.ExtraStage#setOnCloseRequest(Runnable)
	 */
	@Override
	public void setOnCloseRequest(Runnable func) {
		this.stage.setOnCloseRequest(req -> func.run());
	}

	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setOnDragOver()
	 */
	@Override
	public void setOnDragOver() {
		this.stage.getScene().getRoot().setOnDragOver(event -> {
			final Dragboard board = event.getDragboard();
			if (board.hasFiles()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
		});
	}

	/* (非 Javadoc)
	 * @see com.ysrken.kamo.service.ExtraStage#setOnDragDropped(Consumer)
	 */
	@Override
	public <File> void setOnDragDropped(Consumer<File> func) {
		this.stage.getScene().getRoot().setOnDragDropped(event -> {
			final Dragboard board = event.getDragboard();
			if (board.hasFiles()) {
				board.getFiles().forEach(file -> {
					func.accept((File) file);
				});
				event.setDropCompleted(true);
			} else {
				event.setDropCompleted(false);
			}
		});
	}
}
