package com.ysrken.kamo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.ysrken.kamo.service.ExtraStage;
import com.ysrken.kamo.service.ExtraStageImpl;
import com.ysrken.kamo.service.LoggerService;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * 起動用クラス
 * @author ysrken
 */
@ComponentScan
public class MainApp extends Application {

	private LoggerService loggerService = new LoggerService();

    private static ConfigurableApplicationContext context;
    
    /**
     * main関数
     * @param args コマンドライン引数
     * @throws Exception 実行時例外
     */
    public static void main(String[] args) throws Exception {
    	context = new AnnotationConfigApplicationContext(MainApp.class);
        launch(args);
    }

    /**
     * JavaFXの起動処理
     * @param stage Stage情報
     * @throws Exception 実行時例外
     */
    public void start(Stage stage) throws Exception {
    	loggerService.info("Starting Hello JavaFX and Maven demonstration application");
        String fxmlFile = "/fxml/hello.fxml";
        loggerService.debug("Loading FXML for main view from: {}", fxmlFile);
		ExtraStageImpl mainStage = context.getBean(ExtraStage.class, stage, fxmlFile);
        mainStage.setTitle("Hello JavaFX and Maven");
        mainStage.setWidth(400);
        mainStage.setHeight(300);
        mainStage.show();
    }
    
    /**
     * ApplicationContextを引き回すために使用
     * @return 共用するApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
    	return context;
    }
    
    /**
     * JavaFXの終了処理
     * @throws Exception 実行時例外
     */
    @Override
    public void stop() throws Exception {
        context.close();
    }
}
